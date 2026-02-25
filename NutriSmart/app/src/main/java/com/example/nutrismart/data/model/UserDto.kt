package com.example.nutrismart.data.model

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val weight: Double? = null,
    val height: Double? = null,
    val age: Int? = null,
    val targetWeight: Double? = null,
    val stepGoal: Int? = 10000
)