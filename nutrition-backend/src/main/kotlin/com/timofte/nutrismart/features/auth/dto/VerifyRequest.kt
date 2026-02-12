package com.timofte.nutrismart.features.auth.dto

data class VerifyRequest(
    val email: String,
    val code: String
)