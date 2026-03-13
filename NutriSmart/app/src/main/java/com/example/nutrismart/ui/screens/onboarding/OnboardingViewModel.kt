package com.example.nutrismart.ui.screens.onboarding

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
import com.example.nutrismart.data.model.DislikedFood
import com.example.nutrismart.data.model.Gender
import com.example.nutrismart.data.model.MedicalCondition
import com.example.nutrismart.data.model.OnboardingRequest
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OnboardingViewModel : ViewModel() {

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
    var foodSuggestions = mutableStateListOf<String>()
    private var searchJob: Job? = null

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isComplete by mutableStateOf(false)

    var loadingMessage by mutableStateOf("Saving profile...")

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
        searchJob?.cancel()
        
        if (newQuery.length < 1) {
            foodSuggestions.clear()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            try {
                val response = RetrofitClient.api.searchFoods(newQuery)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        foodSuggestions.clear()
                        foodSuggestions.addAll(data)
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }


    fun submitProfile() {
        if (height.isEmpty() || weight.isEmpty() || targetWeight.isEmpty() || budget.isEmpty()) {
            errorMessage = "Please fill in all numeric fields."
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
                    height = height.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    weight = weight.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    targetWeight = targetWeight.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    activityLevel = activityLevel,
                    maxDailyBudget = budget.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    dietaryPreferences = selectedDietaryPreferences.toList(),
                    medicalConditions = selectedMedicalConditions.toList(),
                    dislikedFoods = selectedDislikedFoods.toList(),
                    isImperial = isImperial,
                    currency = currency
                )

                val token = if (UserSession.token.isNotEmpty()) "Bearer ${UserSession.token}" else ""
                val response = RetrofitClient.api.completeProfile(token, request)

                if (response.isSuccessful) {
                    val userId = UserSession.currentUserId
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
                val token = if (UserSession.token.isNotEmpty()) "Bearer ${UserSession.token}" else ""

                val startResponse = RetrofitClient.api.startPlanGeneration(token, userId)

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

                if (index < messages.size - 1) {
                    index++
                }

                delay(6000)
            }
        }
    }

    private suspend fun pollForStatus(userId: Long, messageJob: Job) {
        var isDone = false

        while (!isDone && viewModelScope.isActive) {
            delay(5000)

            try {
                val token = if (UserSession.token.isNotEmpty()) "Bearer ${UserSession.token}" else ""

                val statusResponse = RetrofitClient.api.checkGenerationStatus(token, userId)

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
            }
        }
    }
}