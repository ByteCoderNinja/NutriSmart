package com.example.nutrismart.data.remote

import com.example.nutrismart.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

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

    @POST("nutrition/generate/{userId}")
    suspend fun startPlanGeneration(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<Map<String, String>>

    @GET("nutrition/status/{userId}")
    suspend fun checkGenerationStatus(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<Map<String, String>>
}