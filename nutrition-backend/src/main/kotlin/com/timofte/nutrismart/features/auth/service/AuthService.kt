package com.timofte.nutrismart.features.auth.service

import com.timofte.nutrismart.features.auth.dto.AuthResponse
import com.timofte.nutrismart.features.auth.dto.LoginRequest
import com.timofte.nutrismart.features.auth.dto.RegisterRequest
import com.timofte.nutrismart.features.user.model.UserEntity
import com.timofte.nutrismart.features.user.repository.UserRepository
import com.timofte.nutrismart.features.user.service.UserService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val jwtUtils: JwtUtils
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email already exists")
        }

        val newUser = UserEntity(
            email = request.email,
            username = request.username,
            passwordHash = request.password,
            dateOfBirth = request.dateOfBirth,
            height = request.height,
            weight = request.weight,
            targetWeight = request.targetWeight,
            gender = request.gender,
            activityLevel = request.activityLevel,
            maxDailyBudget = request.maxDailyBudget,
            dietaryPreferences = request.dietaryPreferences,
            medicalConditions = request.medicalConditions
        )

        val savedUser = userService.calculateAndSaveUserNeeds(newUser)

        val token = jwtUtils.generateToken(savedUser.email)

        return AuthResponse(token, savedUser.id)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw RuntimeException("User not found")

        if (user.passwordHash != request.password) {
            throw RuntimeException("Invalid password")
        }

        val token = jwtUtils.generateToken(user.email)
        return AuthResponse(token, user.id)
    }
}