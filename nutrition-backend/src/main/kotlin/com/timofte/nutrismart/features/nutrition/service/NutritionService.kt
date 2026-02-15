package com.timofte.nutrismart.features.nutrition.service

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import com.timofte.nutrismart.features.food.service.MealService
import com.timofte.nutrismart.features.nutrition.model.MealDTO
import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.model.ShoppingList
import com.timofte.nutrismart.features.nutrition.model.ShoppingListItem
import com.timofte.nutrismart.features.nutrition.model.WeeklyPlanDTO
import com.timofte.nutrismart.features.nutrition.repository.MealPlanRepository
import com.timofte.nutrismart.features.nutrition.repository.ShoppingListRepository
import com.timofte.nutrismart.features.user.model.UserEntity
import com.timofte.nutrismart.features.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Service
class NutritionService(
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val userRepository: UserRepository,
    private val geminiService: GeminiService,
    private val mealService: MealService
) {
    private val generationStatus = ConcurrentHashMap<Long, String>()

    @Async
    @Transactional
    fun generateAndSaveWeeklyPlanAsync(userId: Long) {
        if (generationStatus[userId] == "IN_PROGRESS") {
            return
        }

        try {
            generationStatus[userId] = "IN_PROGRESS"

            val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("User not found") }

            val weeklyPlanDTO: WeeklyPlanDTO = geminiService.generateWeeklyPlan(user)

            mealPlanRepository.deleteByUserId(userId)

            mealPlanRepository.flush()
            shoppingListRepository.flush()

            val savedPlans = mutableListOf<MealPlan>()
            val startDate = LocalDate.now()

            weeklyPlanDTO.days.forEach { dayDTO ->
                val planDate = startDate.plusDays((dayDTO.dayNumber - 1).toLong())

                val breakfastMeal = createMealFromDTO(dayDTO.breakfast, MealType.BREAKFAST)
                val lunchMeal = createMealFromDTO(dayDTO.lunch, MealType.LUNCH)
                val dinnerMeal = createMealFromDTO(dayDTO.dinner, MealType.DINNER)
                val snackMeal = createMealFromDTO(dayDTO.snack, MealType.SNACK)

                val totalCal = breakfastMeal.calories + lunchMeal.calories + dinnerMeal.calories + snackMeal.calories
                val totalProt = breakfastMeal.protein + lunchMeal.protein + dinnerMeal.protein + snackMeal.protein
                val totalCarb = breakfastMeal.carbs + lunchMeal.carbs + dinnerMeal.carbs + snackMeal.carbs
                val totalFt = breakfastMeal.fat + lunchMeal.fat + dinnerMeal.fat + snackMeal.fat

                val newPlan = MealPlan(
                    userId = userId,
                    date = planDate,
                    breakfast = breakfastMeal,
                    lunch = lunchMeal,
                    dinner = dinnerMeal,
                    snack = snackMeal,
                    totalCalories = totalCal,
                    totalProtein = totalProt,
                    totalCarbs = totalCarb,
                    totalFat = totalFt,
                    isCompleted = false
                )
                savedPlans.add(newPlan)
            }

            val finalSavedPlans = mealPlanRepository.saveAll(savedPlans)

            generateAndSaveShoppingListFromPlans(user, finalSavedPlans)

            generationStatus[userId] = "COMPLETED"
            println("[ASYNC] Finished generation for user: $userId")

        } catch (e: Exception) {
            generationStatus[userId] = "FAILED"
            println("[ASYNC] Failed generation for user: $userId - Error: ${e.message}")
            e.printStackTrace()
        }
    }

    @Transactional
    fun generateAndSaveShoppingListFromPlans(user: UserEntity, plans: List<MealPlan>) {
        val allIngredients = plans.flatMap { plan ->
            listOfNotNull(
                plan.breakfast?.quantityDetails,
                plan.lunch?.quantityDetails,
                plan.dinner?.quantityDetails,
                plan.snack?.quantityDetails,
            )
        }.filter { it.isNotBlank() }

        if (allIngredients.isEmpty()) return

        val shoppingListDTO = geminiService.generateShoppingList(allIngredients, user)

        val shoppingList = shoppingListRepository.findByUserId(user.id) ?: ShoppingList(
            userId = user.id,
            items = mutableListOf()
        )

        shoppingList.items.clear()

        val itemsEntities = shoppingListDTO.categories.flatMap { category ->
            category.items.map { itemName ->
                ShoppingListItem(
                    category = category.name,
                    name = itemName,
                    isChecked = false,
                    shoppingList = shoppingList
                )
            }
        }.toMutableList()

        shoppingList.items.addAll(itemsEntities)

        shoppingListRepository.save(shoppingList)
    }

    fun updatePlan(mealPlan: MealPlan): MealPlan {
        return mealPlanRepository.save(mealPlan)
    }


    private fun createMealFromDTO(mealDTO: MealDTO, mealType: MealType): Meal {
        return Meal(
            name = mealDTO.name,
            calories = mealDTO.calories,
            protein = mealDTO.protein,
            carbs = mealDTO.carbs,
            fat = mealDTO.fat,
            quantityDetails = mealDTO.quantityDetails,
            type = mealType
        )
    }

    fun getMealPlan(userId: Long, date: LocalDate): MealPlan? {
        val exactPlan =  mealPlanRepository.findByUserIdAndDate(userId, date)

        if (exactPlan != null) return exactPlan

        val allPlans = mealPlanRepository.findByUserId(userId).sortedBy { it.date }

        if (allPlans.isEmpty()) return null

        val firstPlanDate = allPlans.first().date
        val numberOfPlans = allPlans.size

        val daysDiff = ChronoUnit.DAYS.between(firstPlanDate, date)

        if (daysDiff < 0) return allPlans.first()

        val cycleIndex = (daysDiff % numberOfPlans).toInt()
        val cycledPlan = allPlans[cycleIndex]

        return cycledPlan.copy(
            id = 0,
            date = date,
            isCompleted = false
        )
    }

    @Transactional
    fun swapMeal(planId: Long, mealType: MealType, newMealId: Long): MealPlan {
        val plan = mealPlanRepository.findById(planId)
            .orElseThrow { RuntimeException("Plan not found") }

        val newMealCopy = mealService.cloneMealForSwap(newMealId, mealType)

        when (mealType) {
            MealType.BREAKFAST -> plan.breakfast = newMealCopy
            MealType.LUNCH -> plan.lunch = newMealCopy
            MealType.DINNER -> plan.dinner = newMealCopy
            MealType.SNACK -> plan.snack = newMealCopy
        }

        recalculateTotals(plan)
        return mealPlanRepository.save(plan)
    }

    @Transactional
    fun markMealAsConsumed(mealId: Long, isConsumed: Boolean): Meal {
        return mealService.toggleConsumed(mealId, isConsumed)
    }

    private fun recalculateTotals(plan: MealPlan) {
        val meals = listOfNotNull(plan.breakfast, plan.lunch, plan.dinner, plan.snack)

        plan.totalCalories = meals.sumOf { it.calories }
        plan.totalProtein = meals.sumOf { it.protein }
        plan.totalCarbs = meals.sumOf { it.carbs }
        plan.totalFat = meals.sumOf { it.fat }

        plan.isCompleted = meals.all { it.isConsumed }
    }

    fun getUniqueMealsFromHistory(userId: Long): List<Meal> {
        val userPlans = mealPlanRepository.findByUserId(userId)

        val allMeals = userPlans.flatMap { plan ->
            listOfNotNull(plan.breakfast, plan.lunch, plan.dinner, plan.snack)
        }

        return allMeals.distinctBy { it.name }
    }

    fun getMealPlansForUser(userId: Long): List<MealPlan> {
        return mealPlanRepository.findByUserId(userId)
    }

    fun getShoppingList(userId: Long): ShoppingList? {
        return shoppingListRepository.findByUserId(userId)
    }

    fun getGenerationStatus(userId: Long): String {
        return generationStatus[userId] ?: "NOT_STARTED"
    }
}