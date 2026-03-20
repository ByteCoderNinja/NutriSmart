package com.example.nutrismart.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.MealType
import com.example.nutrismart.data.model.UpdateUserRequest
import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.data.repository.AuthRepository
import com.example.nutrismart.data.repository.MealRepository
import com.example.nutrismart.data.repository.UserRepository
import com.example.nutrismart.data.repository.WaterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
    private val userRepository: UserRepository,
    private val mealRepository: MealRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val currentUserId: Long get() = UserSession.currentUserId
    private val token: String get() = UserSession.token ?: ""
    private var updateWeightJob: Job? = null

    init {
        val savedWater = waterRepository.getWaterIntake()
        val wakeUpTimeStr = authRepository.getWakeUpTime()
        val wakeUpTime = try { LocalTime.parse(wakeUpTimeStr) } catch(e: Exception) { LocalTime.of(8,0) }
        _uiState.update { it.copy(waterConsumedMl = savedWater, wakeUpTime = wakeUpTime) }

        if (currentUserId != -1L) {
            fetchTodayData()
        }
    }

    fun fetchTodayData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val localDate = java.time.LocalDate.now().toString()
                
                val planResponse = mealRepository.getDailyPlan(token, currentUserId, localDate)
                val shoppingListResponse = mealRepository.getShoppingList(token, currentUserId)
                val userResponse = userRepository.getUser(token, currentUserId)

                if (userResponse.isSuccessful) {
                    userResponse.body()?.let { user ->
                        _uiState.update { it.copy(
                            weight = user.weight ?: 0.0,
                            isImperial = user.isImperial
                        ) }
                    }
                }

                if (planResponse.isSuccessful) {
                    planResponse.body()?.let { plan ->
                        _uiState.update { it.copy(
                            breakfast = plan.breakfast, 
                            lunch = plan.lunch, 
                            dinner = plan.dinner, 
                            snack = plan.snack
                        ) }
                    }
                }

                if (shoppingListResponse.isSuccessful) {
                    _uiState.update { it.copy(shoppingList = shoppingListResponse.body()) }
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun loadAlternatives(mealTypeStr: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSwapping = true, alternatives = emptyList()) }

            try {
                val mealType = MealType.fromString(mealTypeStr) ?: return@launch
                val response = mealRepository.getMealAlternatives(token, currentUserId, mealType.value)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(alternatives = response.body() ?: emptyList()) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSwapping = false) }
            }
        }
    }

    fun swapMeal(mealTypeStr: String, newMealId: Long) {
        viewModelScope.launch {
            try {
                val mealType = MealType.fromString(mealTypeStr) ?: return@launch
                val response = mealRepository.swapMeal(token, currentUserId, mealType.value, newMealId)

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
            waterRepository.saveWaterIntake(newWater)
            state.copy(waterConsumedMl = newWater)
        }
    }

    fun removeWater() {
        _uiState.update { state ->
            val newWater = (state.waterConsumedMl - state.glassSizeMl).coerceAtLeast(0)
            waterRepository.saveWaterIntake(newWater)
            state.copy(waterConsumedMl = newWater)
        }
    }

    fun toggleMeal(mealId: Long, mealTypeStr: String, isConsumed: Boolean) {
        val mealType = MealType.fromString(mealTypeStr)
        
        _uiState.update { state ->
            when (mealType) {
                MealType.BREAKFAST -> state.copy(breakfast = state.breakfast?.copy(consumed = isConsumed))
                MealType.LUNCH -> state.copy(lunch = state.lunch?.copy(consumed = isConsumed))
                MealType.DINNER -> state.copy(dinner = state.dinner?.copy(consumed = isConsumed))
                MealType.SNACK -> state.copy(snack = state.snack?.copy(consumed = isConsumed))
                else -> state
            }
        }
        viewModelScope.launch {
            try {
                mealRepository.toggleMealConsumed(token, mealId, isConsumed)
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
                mealRepository.toggleShoppingItem(token, itemId, checked)
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
                val response = mealRepository.getMealAlternatives(token, currentUserId, "SNACK")

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
            if (currentUserId == -1L) return
            val requestBody = UpdateUserRequest(weight = newWeight)
            userRepository.patchUser(token, currentUserId, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
