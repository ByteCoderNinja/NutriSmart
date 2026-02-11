package com.timofte.nutrismart.features.food.controller

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import com.timofte.nutrismart.features.food.service.MealService
import com.timofte.nutrismart.features.nutrition.service.NutritionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/meals")
class MealController(
    private val mealService: MealService,
    private val nutritionService: NutritionService
) {
    @PostMapping
    fun createMeal(@RequestBody meal: Meal): Meal {
        return mealService.saveMeal(meal)
    }

    @GetMapping("/alternatives")
    fun getAlternatives(@RequestParam type: MealType): List<Meal> {
        return mealService.getAlternativeMeals(type)
    }

    @GetMapping("/history")
    fun getUserMealHistory(@RequestParam userId: Long): List<Meal> {
        return nutritionService.getUniqueMealsFromHistory(userId)
    }

    @GetMapping
    fun getMeals(@RequestParam(required = false) type: MealType): List<Meal> {
        return mealService.getMealsByType(type)
    }
}