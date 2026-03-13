package com.timofte.nutrismart.features.food.repository

import com.timofte.nutrismart.features.food.model.Food
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FoodRepository : JpaRepository<Food, Long> {
    fun findByNameContainingIgnoreCaseOrderByNameAsc(query: String): List<Food>
}
