package com.timofte.nutrismart.features.food.service

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import com.timofte.nutrismart.features.food.repository.MealRepository
import org.springframework.stereotype.Service

@Service
class MealService(private val mealRepository: MealRepository) {
    fun saveMeal(meal: Meal) : Meal {
        return mealRepository.save(meal)
    }

    fun getAlternativeMeals(type: MealType) : List<Meal> {
        return mealRepository.findByType(type)
    }

    fun getMealById(id: Long) : Meal {
        return mealRepository.findById(id).orElseThrow { RuntimeException("Meal Not Found") }
    }

    fun getMealsByType(type: MealType) : List<Meal> {
        return mealRepository.findByType(type)
    }
}