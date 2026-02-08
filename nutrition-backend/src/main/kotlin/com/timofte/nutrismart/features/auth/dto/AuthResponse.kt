package com.timofte.nutrismart.features.auth.dto

data class AuthResponse(
    val token: String,
    val userId: Long
)