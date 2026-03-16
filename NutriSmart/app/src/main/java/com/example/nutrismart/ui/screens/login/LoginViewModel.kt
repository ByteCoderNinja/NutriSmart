package com.example.nutrismart.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.AuthRequest
import com.example.nutrismart.data.model.GoogleLoginRequest
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)
    var isNewUser by mutableStateOf(false)
    var isVerified by mutableStateOf(true)

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun resetNavigation() {
        loginSuccess = false
    }

    fun login(sessionManager: SessionManager) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = AuthRequest(email, password)
                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful) {
                    val authData = response.body()
                    if (authData != null) {
                        UserSession.currentUserId = authData.userId
                        UserSession.token = authData.token

                        sessionManager.saveAuthToken(authData.token)
                        sessionManager.saveUserId(authData.userId)
                        sessionManager.saveProfileComplete(authData.isProfileComplete)
                        sessionManager.saveIsVerified(authData.isVerified)

                        isNewUser = !authData.isProfileComplete
                        isVerified = authData.isVerified
                    }

                    loginSuccess = true
                    sessionManager.saveIsGoogleUser(false)
                } else {
                    errorMessage = "Login failed: Check data."
                }
            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun loginWithGoogle(idToken: String, sessionManager: SessionManager) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = GoogleLoginRequest(idToken)
                val response = RetrofitClient.api.googleLogin(request)

                if (response.isSuccessful) {
                    val authData = response.body()
                    if (authData != null) {
                        UserSession.currentUserId = authData.userId
                        UserSession.token = authData.token

                        sessionManager.saveAuthToken(authData.token)
                        sessionManager.saveUserId(authData.userId)
                        sessionManager.saveProfileComplete(authData.isProfileComplete)
                        sessionManager.saveIsVerified(authData.isVerified)

                        isNewUser = !authData.isProfileComplete
                        isVerified = authData.isVerified
                        loginSuccess = true
                        sessionManager.saveIsGoogleUser(true)
                    }
                } else {
                    errorMessage = "Google login failed: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error connecting to server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}