package com.example.nutrismart.data.repository

import com.example.nutrismart.data.model.UpdateUserRequest
import com.example.nutrismart.data.model.UserDto
import com.example.nutrismart.data.remote.NutriSmartApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: NutriSmartApi
) {
    suspend fun getUser(token: String, userId: Long): Response<UserDto> {
        return api.getUser("Bearer $token", userId)
    }

    suspend fun patchUser(token: String, userId: Long, request: UpdateUserRequest): Response<UserDto> {
        return api.patchUser("Bearer $token", userId, request)
    }

    suspend fun deleteUser(token: String, userId: Long): Response<Void> {
        return api.deleteUser("Bearer $token", userId)
    }

    suspend fun completeProfile(token: String, request: com.example.nutrismart.data.model.OnboardingRequest): Response<Any> {
        return api.completeProfile("Bearer $token", request)
    }

    suspend fun startPlanGeneration(token: String, userId: Long): Response<Map<String, String>> {
        return api.startPlanGeneration("Bearer $token", userId)
    }

    suspend fun checkGenerationStatus(token: String, userId: Long): Response<Map<String, String>> {
        return api.checkGenerationStatus("Bearer $token", userId)
    }

    suspend fun searchFoods(token: String, query: String): Response<List<String>> {
        return api.searchFoods("Bearer $token", query)
    }
}
