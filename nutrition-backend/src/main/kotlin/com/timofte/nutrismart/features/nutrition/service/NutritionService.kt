package com.timofte.nutrismart.features.nutrition.service

import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.repository.MealPlanRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NutritionService(private val mealPlanRepository: MealPlanRepository) {

    fun savePlan(mealPlan: MealPlan): MealPlan {
        val totalCalories = (mealPlan.breakfast?.calories ?: 0) +
                            (mealPlan.lunch?.calories ?: 0) +
                            (mealPlan.dinner?.calories ?: 0) +
                            (mealPlan.snack?.calories ?: 0)

        val totalProteins = (mealPlan.breakfast?.proteins ?: 0.0) +
                            (mealPlan.lunch?.proteins ?: 0.0) +
                            (mealPlan.dinner?.proteins ?: 0.0) +
                            (mealPlan.snack?.proteins ?: 0.0)

        val totalCarbs = (mealPlan.breakfast?.carbs ?: 0.0) +
                            (mealPlan.lunch?.carbs ?: 0.0) +
                            (mealPlan.dinner?.carbs ?: 0.0) +
                            (mealPlan.snack?.carbs ?: 0.0)

        val totalFats = (mealPlan.breakfast?.fats ?: 0.0) +
                            (mealPlan.lunch?.fats ?: 0.0) +
                            (mealPlan.dinner?.fats ?: 0.0) +
                            (mealPlan.snack?.fats ?: 0.0)

        val updatedPlan = mealPlan.copy(
            totalCalories = totalCalories,
            totalProteins = totalProteins,
            totalCarbs = totalCarbs,
            totalFats = totalFats
        )

        return mealPlanRepository.save(updatedPlan)
    }

    fun getMealPlan(userId: Long, date: LocalDate): MealPlan? {
        return mealPlanRepository.findByUserIdAndDate(userId, date)
    }
}