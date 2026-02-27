package com.example.nutrismart.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.AuthRequest
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
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
                    println("Login success! Token: ${authData?.token}")

                    if (authData != null) {
                        UserSession.currentUserId = authData.userId
                        UserSession.token = authData.token

                        sessionManager.saveAuthToken(authData.token)
                        sessionManager.saveUserId(authData.userId)
                    }

                    loginSuccess = true
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
}