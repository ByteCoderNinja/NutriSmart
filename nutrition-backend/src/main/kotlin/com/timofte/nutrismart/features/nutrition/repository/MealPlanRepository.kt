package com.timofte.nutrismart.features.nutrition.repository

import com.timofte.nutrismart.features.nutrition.model.MealPlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MealPlanRepository : JpaRepository<MealPlan, Long> {
    fun findByUserIdAndDate(userId: Long, date: LocalDate): MealPlan?
    fun findByUserId(userId: Long): List<MealPlan>
}