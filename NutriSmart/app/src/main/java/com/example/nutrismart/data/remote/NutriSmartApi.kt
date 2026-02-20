package com.example.nutrismart.data.remote

import com.example.nutrismart.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class MealDto(
    val id: Long,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val quantityDetails: String?,
    val consumed: Boolean = false
)

data class MealPlanDto(
    val id: Long,
    val breakfast: MealDto?,
    val lunch: MealDto?,
    val dinner: MealDto?,
    val snack: MealDto?
)

data class ShoppingListDto(
    val id: Long,
    val userId: Long,
    val items: List<ShoppingListItemDto>
)

data class ShoppingListItemDto(
    val id: Long,
    val category: String,
    val name: String,
    val isChecked: Boolean
)

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

    @GET("nutrition/today/{userId}")
    suspend fun getTodayPlan(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<MealPlanDto>

    @GET("nutrition/shopping-list/{userId}")
    suspend fun getShoppingList(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<ShoppingListDto>

    @PATCH("nutrition/meal/{mealId}/consume")
    suspend fun toggleMealConsumed(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: Long,
        @Query("consumed") consumed: Boolean
    ): Response<MealDto>

    @PATCH("api/nutrition/shopping-item/{itemId}/check")
    suspend fun toggleShoppingItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Long,
        @Query("isChecked") isChecked: Boolean
    ): retrofit2.Response<Unit>

    @GET("nutrition/alternatives/{userId}")
    suspend fun getMealAlternatives(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("type") mealType: String
    ): Response<List<MealDto>>

    @POST("nutrition/swap/{userId}")
    suspend fun swapMeal(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("mealType") mealType: String,
        @Query("newMealId") newMealId: Long
    ): Response<MealPlanDto>

    @GET("api/nutrition/plan")
    suspend fun getDailyPlan(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long,
        @Query("date") date: String
    ): Response<MealPlanDto>
}