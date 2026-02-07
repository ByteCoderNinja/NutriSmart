package com.timofte.nutrismart.features.food.model

import jakarta.persistence.*

@Entity
@Table(name = "meals")
data class Meal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val calories: Int,

    val proteins: Double,
    val fats: Double,
    val carbs: Double,

    @Enumerated(EnumType.STRING)
    val type: MealType
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}