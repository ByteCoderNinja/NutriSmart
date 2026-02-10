package com.timofte.nutrismart.features.nutrition.service

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
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
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class NutritionService(
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val userRepository: UserRepository,
    private val geminiService: GeminiService
) {

    @Transactional
    fun generateAndSaveWeeklyPlan(userId: Long): List<MealPlan> {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val weeklyPlanDTO: WeeklyPlanDTO = geminiService.generateWeeklyPlan(user)

        mealPlanRepository.deleteByUserId(userId)
        shoppingListRepository.deleteByUserId(userId)

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

        return finalSavedPlans
    }

    private fun generateAndSaveShoppingListFromPlans(user: UserEntity, plans: List<MealPlan>) {
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

        val newShoppingList = ShoppingList(
            userId = user.id,
            items = mutableListOf()
        )

        val itemsEntities = shoppingListDTO.categories.flatMap { category ->
            category.items.map { itemName ->
                ShoppingListItem(
                    category = category.name,
                    name = itemName,
                    isChecked = false,
                    shoppingList = newShoppingList
                )
            }
        }.toMutableList()

        newShoppingList.items.addAll(itemsEntities)

        shoppingListRepository.save(newShoppingList)
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

    fun getMealPlansForUser(userId: Long): List<MealPlan> {
        return mealPlanRepository.findByUserId(userId)
    }

    fun getShoppingList(userId: Long): ShoppingList? {
        return shoppingListRepository.findByUserId(userId)
    }
}