package com.example.nutrismart.data.repository

import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.model.*
import com.example.nutrismart.data.remote.NutriSmartApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: NutriSmartApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: AuthRequest): Response<AuthResponse> {
        return api.login(request)
    }

    suspend fun googleLogin(request: GoogleLoginRequest): Response<AuthResponse> {
        return api.googleLogin(request)
    }

    suspend fun register(request: RegisterRequest): Response<Void> {
        return api.register(request)
    }

    suspend fun verify(request: VerifyRequest): Response<AuthResponse> {
        return api.verify(request)
    }

    suspend fun resendCode(email: String): Response<Void> {
        return api.resendCode(email)
    }

    fun saveAuthData(authData: AuthResponse) {
        sessionManager.saveAuthToken(authData.token)
        sessionManager.saveUserId(authData.userId)
        sessionManager.saveProfileComplete(authData.isProfileComplete)
        sessionManager.saveIsVerified(authData.isVerified)
    }

    fun saveIsGoogleUser(isGoogle: Boolean) {
        sessionManager.saveIsGoogleUser(isGoogle)
    }

    fun getAuthToken(): String? = sessionManager.fetchAuthToken()
    fun getUserId(): Long = sessionManager.fetchUserId()
    fun isProfileComplete(): Boolean = sessionManager.isProfileComplete()
    fun isVerified(): Boolean = sessionManager.isVerified()
    fun getWakeUpTime(): String = sessionManager.getWakeUpTime()
    
    fun saveWakeUpTime(time: String) {
        sessionManager.saveWakeUpTime(time)
    }
    
    fun clearSession() {
        sessionManager.clearSession()
    }
}
