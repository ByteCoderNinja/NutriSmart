package com.timofte.nutrismart.features.auth.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)