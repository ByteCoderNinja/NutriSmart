package com.example.nutrismart.ui.screens.onboarding

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.ActivityLevel
import com.example.nutrismart.data.model.Currency
import com.example.nutrismart.data.model.DietaryPreference
import com.example.nutrismart.data.model.Gender
import com.example.nutrismart.data.model.MedicalCondition
import com.example.nutrismart.data.model.OnboardingRequest
import com.example.nutrismart.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var birthDate by mutableStateOf(LocalDate.now().minusYears(18))
    var gender by mutableStateOf(Gender.MALE)
    var height by mutableStateOf("")
    var weight by mutableStateOf("")

    var targetWeight by mutableStateOf("")
    var activityLevel by mutableStateOf(ActivityLevel.SEDENTARY)
    var budget by mutableStateOf("")

    var isImperial by mutableStateOf(false)
    var currency by mutableStateOf(Currency.RON)

    var selectedDietaryPreferences = mutableStateListOf<DietaryPreference>()
    var selectedMedicalConditions = mutableStateListOf<MedicalCondition>()
    var selectedDislikedFoods by mutableStateOf<Set<String>>(emptySet())

    var foodSearchQuery by mutableStateOf("")
    var isSearchingFoods by mutableStateOf(false)
    var foodSuggestions = mutableStateListOf<String>()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isComplete by mutableStateOf(false)
    var loadingMessage by mutableStateOf("Saving profile...")

    private val token: String get() = UserSession.token ?: ""
    private val userId: Long get() = UserSession.currentUserId

    fun loadUserData() {
        if (userId == -1L) return

        isLoading = true
        loadingMessage = "Loading your details..."

        viewModelScope.launch {
            try {
                val response = userRepository.getUser(token, userId)

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        user.dateOfBirth?.let {
                            runCatching { birthDate = LocalDate.parse(it) }
                        }
                        user.gender?.let { gender = it }
                        user.height?.let { height = it.toString() }
                        user.weight?.let { weight = it.toString() }
                        user.targetWeight?.let { targetWeight = it.toString() }
                        user.activityLevel?.let { activityLevel = it }
                        user.maxDailyBudget?.let { budget = it.toString() }
                        user.currency?.let { currency = it }
                        isImperial = user.isImperial

                        user.dietaryPreferences?.let {
                            selectedDietaryPreferences.clear()
                            selectedDietaryPreferences.addAll(it)
                        }
                        user.medicalConditions?.let {
                            selectedMedicalConditions.clear()
                            selectedMedicalConditions.addAll(it)
                        }
                        user.dislikedFoods?.let {
                            selectedDislikedFoods = it.toSet()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NutriSmart", "Error loading user data", e)
            } finally {
                isLoading = false
            }
        }
    }

    private val messages = listOf(
        "Analyzing your profile...",
        "Calculating target calories...",
        "Generating 14-day AI recipes...",
        "Finalizing your plan...",
        "Do not panic if it looks stuck\nAI is working"
    )

    fun toggleDiet(diet: DietaryPreference) {
        if (selectedDietaryPreferences.contains(diet)) {
            selectedDietaryPreferences.remove(diet)
        } else {
            selectedDietaryPreferences.add(diet)
        }
    }

    fun toggleCondition(condition: MedicalCondition) {
        if (selectedMedicalConditions.contains(condition)) {
            selectedMedicalConditions.remove(condition)
        } else {
            selectedMedicalConditions.add(condition)
        }
    }

    fun toggleDislikedFood(food: String) {
        val currentSet = selectedDislikedFoods.toMutableSet()
        if (currentSet.contains(food)) {
            currentSet.remove(food)
        } else {
            currentSet.add(food)
        }
        selectedDislikedFoods = currentSet
    }

    fun onFoodSearchQueryChanged(newQuery: String) {
        foodSearchQuery = newQuery
        if (newQuery.isBlank()) {
            foodSuggestions.clear()
            return
        }

        searchFoods(newQuery)
    }

    private var searchJob: Job? = null
    private fun searchFoods(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            isSearchingFoods = true
            try {
                Log.d("NutriSmart", "Searching for: $query")
                val response = userRepository.searchFoods(token, query)
                
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("NutriSmart", "Found: ${data?.size} items")
                    foodSuggestions.clear()
                    data?.let { foodSuggestions.addAll(it) }
                } else {
                    Log.e("NutriSmart", "Search failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NutriSmart", "Search error", e)
            } finally {
                isSearchingFoods = false
            }
        }
    }

    fun submitProfile() {
        val hValue = height.replace(',', '.').toDoubleOrNull()
        val wValue = weight.replace(',', '.').toDoubleOrNull()
        val twValue = targetWeight.replace(',', '.').toDoubleOrNull()
        val bValue = budget.replace(',', '.').toDoubleOrNull()

        if (hValue == null || wValue == null || twValue == null || bValue == null) {
            errorMessage = "Please fill in all numeric fields correctly."
            return
        }

        if (birthDate.isAfter(LocalDate.now())) {
            errorMessage = "Birth date cannot be in the future."
            return
        }
        val age = java.time.Period.between(birthDate, LocalDate.now()).years
        if (age < 14) {
            errorMessage = "You must be at least 14 years old to use this app."
            return
        }

        val minHeight = if (isImperial) 3.3 else 100.0
        val maxHeight = if (isImperial) 9.8 else 300.0
        if (hValue !in minHeight..maxHeight) {
            errorMessage = "Height must be between $minHeight and $maxHeight ${if (isImperial) "ft" else "cm"}."
            return
        }

        val minWeight = if (isImperial) 33.0 else 15.0
        val maxWeight = if (isImperial) 661.0 else 300.0
        if (wValue !in minWeight..maxWeight) {
            errorMessage = "Weight must be between $minWeight and $maxWeight ${if (isImperial) "lbs" else "kg"}."
            return
        }
        if (twValue !in minWeight..maxWeight) {
            errorMessage = "Target weight must be between $minWeight and $maxWeight ${if (isImperial) "lbs" else "kg"}."
            return
        }

        val minBudget = if (currency == Currency.RON) 20.0 else 10.0
        if (bValue < minBudget) {
            errorMessage = "Minimum daily budget is $minBudget ${currency.name}."
            return
        }

        isLoading = true
        errorMessage = null
        loadingMessage = "Saving profile..."

        viewModelScope.launch {
            try {
                val dateString = birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val request = OnboardingRequest(
                    dateOfBirth = dateString,
                    gender = gender,
                    height = hValue,
                    weight = wValue,
                    targetWeight = twValue,
                    activityLevel = activityLevel,
                    maxDailyBudget = bValue,
                    dietaryPreferences = selectedDietaryPreferences.toList(),
                    medicalConditions = selectedMedicalConditions.toList(),
                    dislikedFoods = selectedDislikedFoods.toList(),
                    isImperial = isImperial,
                    currency = currency
                )

                val response = userRepository.completeProfile(token, request)

                if (response.isSuccessful) {
                    if (userId == -1L) {
                        errorMessage = "Error: User ID not found in session."
                        isLoading = false
                        return@launch
                    }
                    startPlanGeneration(userId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = if (!errorBody.isNullOrEmpty() && errorBody.contains("message")) {
                        "Error ${response.code()}: AI profile save failed."
                    } else {
                        "Error: ${response.code()} - ${response.message()}"
                    }
                    isLoading = false
                }

            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun startPlanGeneration(userId: Long) {
        viewModelScope.launch {
            try {
                val startResponse = userRepository.startPlanGeneration(token, userId)

                if (startResponse.isSuccessful) {
                    val messageJob = launchMessageRotation()
                    pollForStatus(userId, messageJob)
                } else {
                    errorMessage = "Failed to start AI generation."
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun launchMessageRotation(): Job {
        return viewModelScope.launch {
            var index = 0
            while (isActive) {
                loadingMessage = messages[index]
                if (index < messages.size - 1) index++
                delay(6000)
            }
        }
    }

    private suspend fun pollForStatus(userId: Long, messageJob: Job) {
        var isDone = false
        while (!isDone && viewModelScope.isActive) {
            delay(5000)
            try {
                val statusResponse = userRepository.checkGenerationStatus(token, userId)

                if (statusResponse.isSuccessful) {
                    val status = statusResponse.body()?.get("status") ?: "UNKNOWN"
                    when (status) {
                        "COMPLETED" -> {
                            isDone = true
                            messageJob.cancel()
                            loadingMessage = "Plan generated successfully!"
                            delay(1000)
                            isLoading = false
                            isComplete = true
                        }
                        "FAILED" -> {
                            isDone = true
                            messageJob.cancel()
                            errorMessage = "AI generation failed. Please try again."
                            isLoading = false
                        }
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
