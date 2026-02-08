package com.timofte.nutrismart.features.auth.dto

import com.timofte.nutrismart.features.user.model.*
import java.time.LocalDate

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val height: Double,
    val weight: Double,
    val targetWeight: Double,
    val activityLevel: ActivityLevel,
    val maxDailyBudget: Double,
    val dietaryPreferences: Set<DietaryPreference>,
    val medicalConditions: Set<MedicalCondition>
)