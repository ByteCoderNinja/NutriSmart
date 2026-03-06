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
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.model.UpdateUserRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.roundToInt

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

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val currentUserId: Long get() = UserSession.currentUserId
    private var updateWeightJob: Job? = null

    init {
        val savedWater = sessionManager.getWaterIntake()
        _uiState.update { it.copy(waterConsumedMl = savedWater) }

        if (currentUserId != -1L) {
            fetchTodayData()
        }
    }

    fun fetchTodayData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val authHeader = "Bearer ${UserSession.token}"
                val planResponse = RetrofitClient.api.getTodayPlan(authHeader, currentUserId)
                val shoppingListResponse = RetrofitClient.api.getShoppingList(authHeader, currentUserId)
                val userResponse = RetrofitClient.api.getUser(authHeader, currentUserId)

                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    if (user != null) {
                        _uiState.update { it.copy(weight = user.weight ?: 0.0) }
                    }
                }

                if (planResponse.isSuccessful) {
                    val plan = planResponse.body()
                    _uiState.update { it.copy(breakfast = plan?.breakfast, lunch = plan?.lunch, dinner = plan?.dinner, snack = plan?.snack) }
                }

                if (shoppingListResponse.isSuccessful) {
                    _uiState.update { it.copy(shoppingList = shoppingListResponse.body()) }
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun loadAlternatives(mealType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSwapping = true, alternatives = emptyList()) }

            try {
                val formattedType = mealType.uppercase().trim()
                val response = RetrofitClient.api.getMealAlternatives(
                    "Bearer ${UserSession.token}",
                    currentUserId,
                    formattedType
                )

                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _uiState.update { it.copy(alternatives = list) }
                } else {
                    println("API error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSwapping = false) }
            }
        }
    }

    fun swapMeal(mealType: String, newMealId: Long) {
        viewModelScope.launch {
            try {
                val formattedType = mealType.uppercase().trim()
                val response = RetrofitClient.api.swapMeal(
                    "Bearer ${UserSession.token}",
                    currentUserId,
                    formattedType,
                    newMealId
                )

                if (response.isSuccessful) {
                    fetchTodayData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addWater() {
        _uiState.update { state ->
            val newWater = state.waterConsumedMl + state.glassSizeMl
            sessionManager.saveWaterIntake(newWater)
            state.copy(waterConsumedMl = newWater)
        }
    }

    fun removeWater() {
        _uiState.update { state ->
            val newWater = (state.waterConsumedMl - state.glassSizeMl).coerceAtLeast(0)
            sessionManager.saveWaterIntake(newWater)
            state.copy(waterConsumedMl = newWater)
        }
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
                RetrofitClient.api.toggleMealConsumed("Bearer ${UserSession.token}", mealId, isConsumed)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun toggleShoppingListItem(itemId: Long, checked: Boolean) {
        _uiState.update { currentState ->
            val updatedItems = currentState.shoppingList?.items?.map { if (it.id == itemId) it.copy(checked = checked) else it }
            currentState.copy(shoppingList = currentState.shoppingList?.copy(items = updatedItems ?: emptyList()))
        }
        viewModelScope.launch {
            try {
                RetrofitClient.api.toggleShoppingItem("Bearer ${UserSession.token}", itemId, checked)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateHealthData(realSteps: Int, burnedKcal: Int) {
        _uiState.update { it.copy(steps = realSteps, burnedCalories = burnedKcal) }
    }

    fun updateWaterGoalBasedOnWeather(temperature: Int) {
        _uiState.update { state ->
            val newGoal = if (temperature >= 30) 3000 else 2000
            state.copy(waterGoalMl = newGoal)
        }
    }

    fun loadBonusSnacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSwapping = true, availableBonusSnacks = emptyList()) }
            try {
                val response = RetrofitClient.api.getMealAlternatives(
                    "Bearer ${UserSession.token}",
                    UserSession.currentUserId,
                    "SNACK"
                )

                if (response.isSuccessful) {
                    val allSnacks = response.body() ?: emptyList()
                    val maxCaloriesAllowed = _uiState.value.burnedCalories
                    val filtered = allSnacks.filter { it.calories <= maxCaloriesAllowed }

                    _uiState.update { it.copy(availableBonusSnacks = filtered) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSwapping = false) }
            }
        }
    }

    fun selectBonusSnack(meal: MealDto) {
        _uiState.update { it.copy(bonusSnack = meal) }
    }

    fun toggleBonusSnackConsumed(isConsumed: Boolean) {
        _uiState.update { state ->
            state.copy(bonusSnack = state.bonusSnack?.copy(consumed = isConsumed))
        }
    }

    fun adjustWeight(delta: Double) {
        val currentWeight = _uiState.value.weight
        val newWeight = (currentWeight + delta).coerceAtLeast(30.0)

        val roundedWeight = (newWeight * 10.0).roundToInt() / 10.0

        _uiState.update { it.copy(weight = roundedWeight) }

        updateWeightJob?.cancel()

        updateWeightJob = viewModelScope.launch {
            delay(1500)
            saveWeightToBackend(roundedWeight)
        }
    }

    private suspend fun saveWeightToBackend(newWeight: Double) {
        try {
            val userId = UserSession.currentUserId
            if (userId == -1L) return

            val authHeader = "Bearer ${UserSession.token}"
            val requestBody = UpdateUserRequest(weight = newWeight)

            RetrofitClient.api.patchUser(authHeader, userId, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}