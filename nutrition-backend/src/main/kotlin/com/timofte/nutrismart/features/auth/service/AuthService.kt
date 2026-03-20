package com.timofte.nutrismart.features.auth.service

import com.timofte.nutrismart.features.auth.dto.*
import com.timofte.nutrismart.features.user.model.AuthProvider
import com.timofte.nutrismart.features.user.model.UserEntity
import com.timofte.nutrismart.features.user.repository.UserRepository
import com.timofte.nutrismart.infrastructure.mail.EmailService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Random
import com.timofte.nutrismart.common.exception.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtils: JwtUtils,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val emailService: EmailService,
    private val googleAuthService: GoogleAuthService
) {

    fun register(request: RegisterRequest) {
        if (userRepository.findByEmail(request.email) != null) {
            throw ConflictException("Email already exists: ${request.email}")
        }

        val code = String.format("%06d", Random().nextInt(999999))

        val newUser = UserEntity(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            isProfileComplete = false,
            isVerified = false,
            provider = AuthProvider.LOCAL,
            verificationCode = code,
            verificationCodeExpiresAt = LocalDateTime.now().plusMinutes(15)
        )

        userRepository.save(newUser)
        emailService.sendVerificationCode(newUser.email, code)
    }

    fun verifyEmail(request: VerifyRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw ResourceNotFoundException("User not found")

        if (user.isVerified) {
            return generateAuthResponse(user)
        }

        if (user.verificationCode == request.code &&
            user.verificationCodeExpiresAt != null &&
            user.verificationCodeExpiresAt!!.isAfter(LocalDateTime.now())) {

            user.isVerified = true
            user.verificationCode = null
            user.verificationCodeExpiresAt = null
            userRepository.save(user)

            return generateAuthResponse(user)
        } else {
            throw BadRequestException("Invalid or expired verification code")
        }
    }

    fun login(request: LoginRequest): AuthResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password),
            )
        } catch (e: Exception) {
            throw UnauthorizedException("Invalid email or password")
        }

        val user = userRepository.findByEmail(request.email)
            ?: throw ResourceNotFoundException("User not found")

        return generateAuthResponse(user)
    }

    fun resendVerificationCode(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (user.isVerified) {
            throw BadRequestException("Account is already verified")
        }

        val code = String.format("%06d", Random().nextInt(999999))
        user.verificationCode = code
        user.verificationCodeExpiresAt = LocalDateTime.now().plusMinutes(15)
        userRepository.save(user)

        emailService.sendVerificationCode(user.email, code)
    }

    fun googleLogin(request: GoogleLoginRequest): AuthResponse {
        val payload = googleAuthService.verifyToken(request.token)
            ?: throw UnauthorizedException("Invalid Google ID Token")

        val email = payload.email
        val name = payload["name"] as String? ?: payload.email.split("@")[0]

        var user = userRepository.findByEmail(email)

        if (user == null) {
            user = UserEntity(
                email = email,
                username = name,
                passwordHash = null,
                isProfileComplete = false,
                isVerified = true,
                provider = AuthProvider.GOOGLE
            )
            userRepository.save(user)
        }

        return generateAuthResponse(user!!)
    }

    private fun generateAuthResponse(user: UserEntity): AuthResponse {
        val userDetails = User.withUsername(user.email)
            .password(user.passwordHash ?: "")
            .roles("USER")
            .build()

        val token = jwtUtils.generateToken(userDetails)
        return AuthResponse(token, user.id, user.isProfileComplete, user.isVerified)
    }

    fun processForgotPassword(email: String) {
        val user = userRepository.findByEmail(email) 
            ?: throw ResourceNotFoundException("User with this email does not exist.")

        val resetCode = String.format("%06d", Random().nextInt(999999))

        user.verificationCode = resetCode
        user.verificationCodeExpiresAt = LocalDateTime.now().plusMinutes(15)
        userRepository.save(user)

        emailService.sendVerificationCode(user.email, resetCode)
    }

    fun resetUserPassword(request: ResetPasswordRequest) {
        val user = userRepository.findByEmail(request.email)
            ?: throw ResourceNotFoundException("User not found")

        if (user.verificationCode != request.code ||
            user.verificationCodeExpiresAt == null ||
            user.verificationCodeExpiresAt!!.isBefore(LocalDateTime.now())) {
            throw BadRequestException("Invalid or expired verification code")
        }

        if (passwordEncoder.matches(request.newPassword, user.passwordHash)) {
            throw BadRequestException("New password cannot be the same as the old password")
        }

        user.passwordHash = passwordEncoder.encode(request.newPassword)
        user.verificationCode = null
        user.verificationCodeExpiresAt = null

        userRepository.save(user)
    }
}
