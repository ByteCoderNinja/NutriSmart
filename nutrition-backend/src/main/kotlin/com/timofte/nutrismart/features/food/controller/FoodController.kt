package com.timofte.nutrismart.features.food.controller

import com.timofte.nutrismart.common.ApiResponse
import com.timofte.nutrismart.features.food.service.FoodService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/foods")
class FoodController(private val foodService: FoodService) {

    @GetMapping("/search")
    fun searchFoods(@RequestParam query: String): ResponseEntity<List<String>> {
        val results = foodService.searchFoods(query)
        return ResponseEntity.ok(results)
    }
}
