package com.timofte.nutrismart.features.food.repository

import com.timofte.nutrismart.features.food.model.Meal
import com.timofte.nutrismart.features.food.model.MealType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MealRepository : JpaRepository<Meal, Long> {
    fun findByType(type: MealType) : List<Meal>
}