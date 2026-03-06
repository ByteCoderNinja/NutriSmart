package com.example.nutrismart.data.model

data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null,
    val weight: Double? = null
)