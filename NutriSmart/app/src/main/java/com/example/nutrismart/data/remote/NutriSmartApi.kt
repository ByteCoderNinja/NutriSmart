package com.example.nutrismart.data.remote

import com.example.nutrismart.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NutriSmartApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/verify")
    suspend fun verify(@Body request: VerifyRequest): Response<AuthResponse>

    @POST("users/onboarding")
    suspend fun completeProfile(
        @Header("Authorization") token: String,
        @Body request: OnboardingRequest
    ): Response<Any>
}