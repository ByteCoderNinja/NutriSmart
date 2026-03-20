package com.example.nutrismart.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.AuthRequest
import com.example.nutrismart.data.model.GoogleLoginRequest
import com.example.nutrismart.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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

    fun login() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = AuthRequest(email, password)
                val response = authRepository.login(request)

                if (response.isSuccessful) {
                    response.body()?.let { authData ->
                        UserSession.currentUserId = authData.userId
                        UserSession.token = authData.token

                        authRepository.saveAuthData(authData)
                        authRepository.saveIsGoogleUser(false)

                        isNewUser = !authData.isProfileComplete
                        isVerified = authData.isVerified
                        loginSuccess = true
                    }
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

    fun loginWithGoogle(idToken: String) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = GoogleLoginRequest(idToken)
                val response = authRepository.googleLogin(request)

                if (response.isSuccessful) {
                    response.body()?.let { authData ->
                        UserSession.currentUserId = authData.userId
                        UserSession.token = authData.token

                        authRepository.saveAuthData(authData)
                        authRepository.saveIsGoogleUser(true)

                        isNewUser = !authData.isProfileComplete
                        isVerified = authData.isVerified
                        loginSuccess = true
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
