package com.example.nutrismart.ui.screens.onboarding

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.model.*
import com.example.nutrismart.data.remote.RetrofitClient
import com.example.nutrismart.ui.screens.verify.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
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

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isComplete by mutableStateOf(false)

    var loadingMessage by mutableStateOf("Saving profile...")

    private val messages = listOf(
        "Analyzing your profile...",
        "Calculating target calories...",
        "Generating 14-day AI recipes...",
        "Adding items to your shopping list...",
        "Finalizing your plan..."
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
                    height = height.toDoubleOrNull() ?: 0.0,
                    weight = weight.toDoubleOrNull() ?: 0.0,
                    targetWeight = targetWeight.toDoubleOrNull() ?: 0.0,
                    activityLevel = activityLevel,
                    maxDailyBudget = budget.toDoubleOrNull() ?: 0.0,
                    dietaryPreferences = selectedDietaryPreferences.toList(),
                    medicalConditions = selectedMedicalConditions.toList(),
                    isImperial = isImperial,
                    currency = currency
                )

                val token = SessionManager.token ?: ""
                val response = RetrofitClient.api.completeProfile(token, request)

                if (response.isSuccessful) {
                    val userId = SessionManager.userId ?: throw Exception("User ID not found")
                    startPlanGeneration(userId)
                } else {
                    errorMessage = "Error: ${response.code()} - ${response.message()}"
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
                val token = SessionManager.token ?: ""

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
                loadingMessage = messages[index % messages.size]
                index++
                delay(8000)
            }
        }
    }

    private suspend fun pollForStatus(userId: Long, messageJob: Job) {
        var isDone = false

        while (!isDone && viewModelScope.isActive) {
            delay(5000)

            try {
                val token = SessionManager.token ?: ""

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