package com.timofte.nutrismart.features.auth.dto

data class LoginRequest(
    val email: String,
    val password: String
)