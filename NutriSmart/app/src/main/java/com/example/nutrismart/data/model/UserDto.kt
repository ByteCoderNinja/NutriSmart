package com.example.nutrismart.data.model

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val weight: Double? = null,
    val height: Double? = null,
    val dateOfBirth: String? = null,
    val targetWeight: Double? = null,
    val gender: Gender? = null,
    val activityLevel: ActivityLevel? = null,
    val maxDailyBudget: Double? = null,
    val dietaryPreferences: List<DietaryPreference>? = null,
    val medicalConditions: List<MedicalCondition>? = null,
    val dislikedFoods: List<String>? = null,
    val stepGoal: Int? = 10000,
    val isGoogleUser: Boolean = false,
    val isImperial: Boolean = false,
    val currency: Currency? = null
)
