package com.timofte.nutrismart.features.auth.service

import com.timofte.nutrismart.features.auth.dto.AuthResponse
import com.timofte.nutrismart.features.auth.dto.LoginRequest
import com.timofte.nutrismart.features.auth.dto.RegisterRequest
import com.timofte.nutrismart.features.user.model.UserEntity
import com.timofte.nutrismart.features.user.repository.UserRepository
import com.timofte.nutrismart.features.user.service.UserService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email already exists: ${request.email}")
        }

        val newUser = UserEntity(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            isProfileComplete = false
        )

        val savedUser = userRepository.save(newUser)

        val userDetails = org.springframework.security.core.userdetails.User
            .withUsername(savedUser.email)
            .password(savedUser.passwordHash)
            .roles("USER")
            .build()

        val token = jwtUtils.generateToken(userDetails)

        return AuthResponse(token, savedUser.id)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password),
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw RuntimeException("User not found")

        val userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.email)
            .password(user.passwordHash)
            .roles("USER")
            .build()

        val token = jwtUtils.generateToken(userDetails)

        return AuthResponse(token, user.id)
    }
}