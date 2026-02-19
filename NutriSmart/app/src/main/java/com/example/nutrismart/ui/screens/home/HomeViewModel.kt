package com.example.nutrismart.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.data.remote.RetrofitClient
import com.example.nutrismart.data.remote.ShoppingListDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,

    val steps: Int = 4520,
    val stepsGoal: Int = 10000,
    val waterConsumedMl: Int = 500,
    val glassSizeMl: Int = 250,

    val breakfast: MealDto? = null,
    val lunch: MealDto? = null,
    val dinner: MealDto? = null,
    val snack: MealDto? = null,
    val shoppingList: ShoppingListDto? = null
) {
    val totalCaloriesGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.calories }.coerceAtLeast(1)
    val carbsGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.carbs }.coerceAtLeast(1)
    val proteinGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.protein }.coerceAtLeast(1)
    val fatGoal: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).sumOf { it.fat }.coerceAtLeast(1)

    val caloriesConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).filter { it.consumed }.sumOf { it.calories }
    val carbsConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).filter { it.consumed }.sumOf { it.carbs }
    val proteinConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).filter { it.consumed }.sumOf { it.protein }
    val fatConsumed: Int get() = listOfNotNull(breakfast, lunch, dinner, snack).filter { it.consumed }.sumOf { it.fat }

    val caloriesRemaining: Int get() = totalCaloriesGoal - caloriesConsumed
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentUserId: Long get() = UserSession.currentUserId

    init {
        if (currentUserId != -1L) {
            fetchTodayData()
        } else {
            println("ERROR")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun fetchTodayData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val authHeader = "Bearer ${UserSession.token}"

                val planResponse = RetrofitClient.api.getTodayPlan(authHeader, currentUserId)
                val shoppingListResponse = RetrofitClient.api.getShoppingList(authHeader, currentUserId)

                if (planResponse.isSuccessful) {
                    val plan = planResponse.body()
                    _uiState.update { state ->
                        state.copy(
                            breakfast = plan?.breakfast,
                            lunch = plan?.lunch,
                            dinner = plan?.dinner,
                            snack = plan?.snack
                        )
                    }
                } else {
                    println("API Meal Error: Code ${planResponse.code()}")
                }

                if (shoppingListResponse.isSuccessful) {
                    _uiState.update { it.copy(shoppingList = shoppingListResponse.body()) }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addWater() {
        _uiState.update { it.copy(waterConsumedMl = it.waterConsumedMl + it.glassSizeMl) }
    }

    fun toggleMeal(mealId: Long, mealType: String, isConsumed: Boolean) {
        _uiState.update { state ->
            when (mealType) {
                "Breakfast" -> state.copy(breakfast = state.breakfast?.copy(consumed = isConsumed))
                "Lunch" -> state.copy(lunch = state.lunch?.copy(consumed = isConsumed))
                "Dinner" -> state.copy(dinner = state.dinner?.copy(consumed = isConsumed))
                "Snack" -> state.copy(snack = state.snack?.copy(consumed = isConsumed))
                else -> state
            }
        }

        viewModelScope.launch {
            try {
                val authHeader = "Bearer ${UserSession.token}"
                val response = RetrofitClient.api.toggleMealConsumed(authHeader, mealId, isConsumed)

                if (!response.isSuccessful) {
                    println("Server error when meal is checked/unchecked: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleShoppingListItem(itemId: Long, isChecked: Boolean) {
        _uiState.update { currentState ->
            val updatedShoppingListItems = currentState.shoppingList?.items?.map { item ->
                if (item.id == itemId) item.copy(isChecked = isChecked) else item
            }
            currentState.copy(
                shoppingList = currentState.shoppingList?.copy(
                    items = updatedShoppingListItems ?: emptyList()
                )
            )
        }

        viewModelScope.launch {
            try {
                val authHeader = "Bearer ${UserSession.token}"
                val response = RetrofitClient.api.toggleShoppingItem(authHeader, itemId, isChecked)
                if (!response.isSuccessful) {
                    println("Server error updating shopping item: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}