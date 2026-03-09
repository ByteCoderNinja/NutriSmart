package com.example.nutrismart.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val weight: Double? = null,
    val height: Double? = null,
    val dateOfBirth: String? = null,
    val targetWeight: Double? = null,
    val stepGoal: Int? = 10000,
    val isGoogleUser: Boolean = false,
    val isImperial: Boolean = false
)