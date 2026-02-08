package com.timofte.nutrismart.features.nutrition.service

import com.timofte.nutrismart.features.food.repository.MealRepository
import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.repository.MealPlanRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NutritionService(
    private val mealPlanRepository: MealPlanRepository,
    private val mealRepository: MealRepository
    ) {

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

        val totalProteins = (breakfast?.protein ?: 0.0) +
                            (lunch?.protein ?: 0.0) +
                            (dinner?.protein ?: 0.0) +
                            (snack?.protein ?: 0.0)

        val totalCarbs = (breakfast?.carbs ?: 0.0) +
                            (lunch?.carbs ?: 0.0) +
                            (dinner?.carbs ?: 0.0) +
                            (snack?.carbs ?: 0.0)

        val totalFats = (breakfast?.fat ?: 0.0) +
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
            totalProteins = totalProteins,
            totalCarbs = totalCarbs,
            totalFats = totalFats
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