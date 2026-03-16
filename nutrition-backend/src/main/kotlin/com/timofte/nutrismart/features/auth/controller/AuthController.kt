package com.timofte.nutrismart.features.auth.controller

import com.timofte.nutrismart.common.ApiResponse
import com.timofte.nutrismart.features.auth.dto.*
import com.timofte.nutrismart.features.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<String>> {
        authService.register(request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Verification code sent to email",
                data = null
            )
        )
    }

    @PostMapping("/verify")
    fun verify(@RequestBody request: VerifyRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.verifyEmail(request))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @PostMapping("/resend-code")
    fun resendCode(@RequestParam email: String): ResponseEntity<ApiResponse<String>> {
        authService.resendVerificationCode(email)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "New verification code sent to email",
                data = null
            )
        )
    }

    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.googleLogin(request))
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse<String>> {
        authService.processForgotPassword(request.email)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Verification code sent if email exists.",
                data = null
            )
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<String>> {
        try {
            authService.resetUserPassword(request)
            return ResponseEntity.ok(ApiResponse(success = true, message = "Password successfully reset.", data = null))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(ApiResponse(success = false, message = e.message ?: "Error", data = null))
        }
    }
}