package com.example.nutrismart.ui.screens.verify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.VerifyRequest
import com.example.nutrismart.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var code by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isResending by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var verificationSuccess by mutableStateOf(false)

    fun verify(email: String) {
        isLoading = true
        errorMessage = null
        successMessage = null
        viewModelScope.launch {
            try {
                val request = VerifyRequest(email, code)
                val response = authRepository.verify(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    UserSession.token = authResponse.token
                    UserSession.currentUserId = authResponse.userId
                    authRepository.saveAuthData(authResponse)
                    verificationSuccess = true
                } else {
                    errorMessage = "Invalid or expired code"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resendCode(email: String) {
        isResending = true
        errorMessage = null
        successMessage = null
        viewModelScope.launch {
            try {
                val response = authRepository.resendCode(email)
                if (response.isSuccessful) {
                    successMessage = "A new code has been sent to your email"
                } else {
                    errorMessage = "Failed to resend code. Try again later."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isResending = false
            }
        }
    }
}
