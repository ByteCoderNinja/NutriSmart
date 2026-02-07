package com.timofte.nutrismart.features.nutrition.model

import com.timofte.nutrismart.features.food.model.Meal
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "meal_plans")
data class MealPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "breakfast_id")
    val breakfast: Meal? = null,

    @ManyToOne
    @JoinColumn(name = "lunch_id")
    val lunch: Meal? = null,

    @ManyToOne
    @JoinColumn(name = "dinner_id")
    val dinner: Meal? = null,

    @ManyToOne
    @JoinColumn(name = "snack_id")
    val snack: Meal? = null,

    val totalCalories: Int = 0,
    val totalProteins: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFats: Double = 0.0,

    val isCompleted: Boolean = false
)