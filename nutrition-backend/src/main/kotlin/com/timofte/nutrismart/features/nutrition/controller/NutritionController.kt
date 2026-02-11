package com.timofte.nutrismart.features.nutrition.controller

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.model.ShoppingList
import com.timofte.nutrismart.features.nutrition.service.NutritionService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/nutrition")
class NutritionController(private val nutritionService: NutritionService) {

    @PostMapping("/generate/{userId}")
    fun generatePlan(@PathVariable userId: Long): ResponseEntity<List<MealPlan>> {
        return try {
            val plans = nutritionService.generateAndSaveWeeklyPlan(userId)
            ResponseEntity.ok(plans)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/plan")
    fun getDailyPlan(
        @RequestParam userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<MealPlan> {
        val plan = nutritionService.getMealPlan(userId, date)
        return if (plan != null) ResponseEntity.ok(plan) else ResponseEntity.notFound().build()
    }

    @PostMapping("/plan")
    fun updateDailyPlan(@RequestBody mealPlan: MealPlan): ResponseEntity<MealPlan> {
        val savedPlan = nutritionService.updatePlan(mealPlan)
        return ResponseEntity.ok(savedPlan)
    }

    @GetMapping("/all/{userId}")
    fun getAllPlans(@PathVariable userId: Long): ResponseEntity<List<MealPlan>> {
        val userPlans = nutritionService.getMealPlansForUser(userId)

        if (userPlans.isEmpty()) return ResponseEntity.noContent().build()

        return ResponseEntity.ok(userPlans)
    }

    @GetMapping("/today/{userId}")
    fun getTodayPlan(@PathVariable userId: Long): ResponseEntity<MealPlan> {
        val today = LocalDate.now()
        val plan = nutritionService.getMealPlan(userId, today)
        return if (plan != null) ResponseEntity.ok(plan) else ResponseEntity.notFound().build()
    }

    @GetMapping("/shopping-list/{userId}")
    fun getShoppingList(@PathVariable userId: Long): ResponseEntity<ShoppingList> {
        val list = nutritionService.getShoppingList(userId)

        return if (list != null) ResponseEntity.ok(list) else ResponseEntity.notFound().build()
    }

    @PostMapping("/swap")
    fun swapMeal(
        @RequestParam planId: Long,
        @RequestParam type: MealType,
        @RequestParam newMealId: Long
    ): ResponseEntity<MealPlan> {
        val updatedPlan = nutritionService.swapMeal(planId, type, newMealId)
        return ResponseEntity.ok(updatedPlan)
    }

    @PatchMapping("/meal/{mealId}/consume")
    fun toggleMealConsumed(
        @PathVariable mealId: Long,
        @RequestParam consumed: Boolean
    ): ResponseEntity<Meal> {
        val updatedMeal = nutritionService.markMealAsConsumed(mealId, consumed)
        return ResponseEntity.ok(updatedMeal)
    }
}