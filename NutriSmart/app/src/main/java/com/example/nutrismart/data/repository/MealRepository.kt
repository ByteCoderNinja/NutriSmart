package com.example.nutrismart.data.repository

import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.data.remote.MealPlanDto
import com.example.nutrismart.data.remote.NutriSmartApi
import com.example.nutrismart.data.remote.ShoppingListDto
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val api: NutriSmartApi
) {
    suspend fun getDailyPlan(token: String, userId: Long, date: String): Response<MealPlanDto> {
        return api.getDailyPlan("Bearer $token", userId, date)
    }

    suspend fun getShoppingList(token: String, userId: Long): Response<ShoppingListDto> {
        return api.getShoppingList("Bearer $token", userId)
    }

    suspend fun getMealAlternatives(token: String, userId: Long, type: String): Response<List<MealDto>> {
        return api.getMealAlternatives("Bearer $token", userId, type)
    }

    suspend fun swapMeal(token: String, userId: Long, type: String, mealId: Long, date: String): Response<MealPlanDto> {
        return api.swapMeal("Bearer $token", userId, type, mealId, date)
    }

    suspend fun toggleMealConsumed(token: String, mealId: Long, consumed: Boolean): Response<MealDto> {
        return api.toggleMealConsumed("Bearer $token", mealId, consumed)
    }

    suspend fun toggleShoppingItem(token: String, itemId: Long, checked: Boolean): Response<Unit> {
        return api.toggleShoppingItem("Bearer $token", itemId, checked)
    }
}
