package com.timofte.nutrismart.features.nutrition.controller

import com.timofte.nutrismart.features.nutrition.model.MealPlan
import com.timofte.nutrismart.features.nutrition.service.NutritionService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/nutrition")
class NutritionController(private val nutritionService: NutritionService) {
    @GetMapping("/plan")
    fun getDailyPlan(
        @RequestParam userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) date: LocalDate,
    ): ResponseEntity<MealPlan> {

        val plan = nutritionService.getMealPlan(userId, date)

        return if (plan != null) {
            ResponseEntity.ok(plan)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllPlans(): ResponseEntity<List<MealPlan>> {
        return ResponseEntity.ok(nutritionService.getMealPlans())
    }

    @PostMapping("/plan")
    fun saveDailyPlan(@RequestBody mealPlan: MealPlan): ResponseEntity<MealPlan> {
        val savedPlan = nutritionService.savePlan(mealPlan)
        return ResponseEntity.ok(savedPlan)
    }

    @PostMapping("/generate/{userId}")
    fun generateWeeklyPlan(@PathVariable userId: Long): ResponseEntity<List<MealPlan>> {
        try {
            val plans = nutritionService.generateAndSaveWeeklyPlan(userId)
            return ResponseEntity.ok(plans)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/today/{userId}")
    fun getTodayPlan(@PathVariable userId: Long): ResponseEntity<MealPlan> {
        val today = LocalDate.now()
        val plan = nutritionService.getMealPlan(userId, today)

        return if (plan != null) {
            ResponseEntity.ok(plan)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/weekly/{userId}")
    fun getWeeklyPlan(@PathVariable userId: Long): ResponseEntity<List<MealPlan>> {
        val allPlans = nutritionService.getMealPlans().filter { it.userId == userId }
        return ResponseEntity.ok(allPlans)
    }
}