package com.example.nutrismart.ui.screens.home

import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.data.remote.ShoppingListDto
import java.time.LocalTime

data class HomeUiState(
    val isLoading: Boolean = true,
    val isSwapping: Boolean = false,
    val steps: Int = 0,
    val stepsGoal: Int = 10000,
    val waterConsumedMl: Int = 0,
    val glassSizeMl: Int = 250,
    val waterGoalMl: Int = 2000,
    val breakfast: MealDto? = null,
    val lunch: MealDto? = null,
    val dinner: MealDto? = null,
    val snack: MealDto? = null,
    val bonusSnack: MealDto? = null,
    val availableBonusSnacks: List<MealDto> = emptyList(),
    val shoppingList: ShoppingListDto? = null,
    val alternatives: List<MealDto> = emptyList(),
    val wakeUpTime: LocalTime = LocalTime.of(8,0),
    val burnedCalories: Int = 0,
    val weight: Double = 0.0
) {
    val totalCaloriesGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.calories }.coerceAtLeast(1)
    val carbsGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.carbs }.coerceAtLeast(1)
    val proteinGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.protein }.coerceAtLeast(1)
    val fatGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.fat }.coerceAtLeast(1)

    val caloriesConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack, bonusSnack).filter { it.consumed }.sumOf { it.calories }
    val carbsConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack, bonusSnack).filter { it.consumed }.sumOf { it.carbs }
    val proteinConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack, bonusSnack).filter { it.consumed }.sumOf { it.protein }
    val fatConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack, bonusSnack).filter { it.consumed }.sumOf { it.fat }
    val caloriesRemaining: Int get() = totalCaloriesGoal - caloriesConsumed + burnedCalories
}
