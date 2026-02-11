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

    @ManyToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "breakfast_id")
    var breakfast: Meal? = null,

    @ManyToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "lunch_id")
    var lunch: Meal? = null,

    @ManyToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "dinner_id")
    var dinner: Meal? = null,

    @ManyToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "snack_id")
    var snack: Meal? = null,

    var totalCalories: Int = 0,
    var totalProtein: Double = 0.0,
    var totalCarbs: Double = 0.0,
    var totalFat: Double = 0.0,

    var isCompleted: Boolean = false
)