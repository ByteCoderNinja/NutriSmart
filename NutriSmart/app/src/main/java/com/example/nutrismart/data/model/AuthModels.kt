package com.example.nutrismart.data.model

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

data class VerifyRequest(
    val email: String,
    val code: String
)

data class AuthResponse(
    val token: String,
    val userId: Long
)

data class OnboardingRequest(
    val dateOfBirth: String,
    val gender: Gender,
    val height: Double,
    val weight: Double,
    val targetWeight: Double,
    val activityLevel: ActivityLevel,
    val maxDailyBudget: Double,
    val dietaryPreferences: List<DietaryPreference>,
    val medicalConditions: List<MedicalCondition>,
    val isImperial: Boolean = false,
    val currency: Currency = Currency.RON
)