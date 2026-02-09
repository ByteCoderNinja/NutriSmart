package com.timofte.nutrismart.features.nutrition.service

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import com.timofte.nutrismart.features.food.repository.MealRepository
import com.timofte.nutrismart.features.nutrition.model.MealDTO
import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.repository.MealPlanRepository
import com.timofte.nutrismart.features.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NutritionService(
    private val mealPlanRepository: MealPlanRepository,
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository,
    private val geminiService: GeminiService,
    ) {

    @Transactional
    fun generateAndSaveWeeklyPlan(userId: Long): List<MealPlan> {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val weeklyPlanDTO = geminiService.generateWeeklyPlan(user)

        val savedPlans = mutableListOf<MealPlan>()
        val startDate = LocalDate.now()

        weeklyPlanDTO.days.forEach { dayDTO ->
            val breakfastEntity = saveMealFromDTO(dayDTO.breakfast, MealType.BREAKFAST)
            val lunchEntity = saveMealFromDTO(dayDTO.lunch, MealType.LUNCH)
            val dinnerEntity = saveMealFromDTO(dayDTO.dinner, MealType.DINNER)
            val snackEntity = saveMealFromDTO(dayDTO.snack, MealType.SNACK)

            val planDate = startDate.plusDays((dayDTO.dayNumber - 1).toLong())

            val newPlan = MealPlan(
                id = 0,
                userId = userId,
                date = planDate,
                breakfast = breakfastEntity,
                lunch = lunchEntity,
                dinner = dinnerEntity,
                snack = snackEntity,
                totalCalories = 0,
                totalProtein = 0.0,
                totalCarbs = 0.0,
                totalFat = 0.0
            )

            val saved = savePlan(newPlan)
            savedPlans.add(saved)
        }

        return savedPlans
    }

    private fun saveMealFromDTO(mealDTO: MealDTO, mealType: MealType): Meal {
        val meal = Meal(
            name = mealDTO.name,
            calories = mealDTO.calories,
            protein = mealDTO.protein,
            carbs = mealDTO.carbs,
            fat = mealDTO.fat,
            quantityDetails = mealDTO.quantityDetails,
            type = mealType
        )
        return mealRepository.save(meal)
    }

    fun savePlan(mealPlan: MealPlan): MealPlan {
        val existingPlan = mealPlanRepository.findByUserIdAndDate(mealPlan.userId, mealPlan.date)

        val breakfast = mealPlan.breakfast?.id?.let { mealRepository.findById(it).orElse(null) }
        val lunch = mealPlan.lunch?.id?.let { mealRepository.findById(it).orElse(null) }
        val dinner = mealPlan.dinner?.id?.let { mealRepository.findById(it).orElse(null) }
        val snack = mealPlan.snack?.id?.let { mealRepository.findById(it).orElse(null) }

        val totalCalories = (breakfast?.calories ?: 0) +
                            (lunch?.calories ?: 0) +
                            (dinner?.calories ?: 0) +
                            (snack?.calories ?: 0)

        val totalProtein = (breakfast?.protein ?: 0.0) +
                           (lunch?.protein ?: 0.0) +
                           (dinner?.protein ?: 0.0) +
                           (snack?.protein ?: 0.0)

        val totalCarbs = (breakfast?.carbs ?: 0.0) +
                         (lunch?.carbs ?: 0.0) +
                         (dinner?.carbs ?: 0.0) +
                         (snack?.carbs ?: 0.0)

        val totalFat = (breakfast?.fat ?: 0.0) +
                       (lunch?.fat ?: 0.0) +
                       (dinner?.fat ?: 0.0) +
                       (snack?.fat ?: 0.0)

        val idToUse = existingPlan?.id ?: mealPlan.id

        val updatedPlan = mealPlan.copy(
            id = idToUse,
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner,
            snack = snack,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat
        )

        return mealPlanRepository.save(updatedPlan)
    }

    fun getMealPlan(userId: Long, date: LocalDate): MealPlan? {
        return mealPlanRepository.findByUserIdAndDate(userId, date)
    }

    fun getMealPlans(): List<MealPlan> {
        return mealPlanRepository.findAll()
    }
}