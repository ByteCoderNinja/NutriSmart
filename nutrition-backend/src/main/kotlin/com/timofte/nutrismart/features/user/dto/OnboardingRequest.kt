package com.timofte.nutrismart.features.user.dto

import com.timofte.nutrismart.features.user.model.ActivityLevel
import com.timofte.nutrismart.features.user.model.Currency
import com.timofte.nutrismart.features.user.model.DietaryPreference
import com.timofte.nutrismart.features.user.model.Gender
import com.timofte.nutrismart.features.user.model.MedicalCondition
import java.time.LocalDate

data class OnboardingRequest(
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val height: Double,
    val weight: Double,
    val targetWeight: Double,
    val activityLevel: ActivityLevel,
    val maxDailyBudget: Double,
    val dietaryPreferences: Set<DietaryPreference>,
    val medicalConditions: Set<MedicalCondition>,
    val isImperial: Boolean,
    val currency: Currency
)