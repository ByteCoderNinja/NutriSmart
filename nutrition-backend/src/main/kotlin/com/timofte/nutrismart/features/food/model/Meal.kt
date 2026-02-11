package com.timofte.nutrismart.features.food.model

import jakarta.persistence.*

@Entity
@Table(name = "meals")
data class Meal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val calories: Int = 0,

    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,

    @Column(length = 1000)
    val quantityDetails: String = "Standard portion",

    @Enumerated(EnumType.STRING)
    val type: MealType = MealType.BREAKFAST,

    var isConsumed: Boolean = false
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}