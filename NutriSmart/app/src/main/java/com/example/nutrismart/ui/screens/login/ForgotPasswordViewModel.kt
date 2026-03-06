package com.example.nutrismart.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var emailSentSuccess by mutableStateOf(false)
    var passwordResetSuccess by mutableStateOf(false)

    fun sendForgotPasswordEmail(email: String) {
        if (email.isBlank()) {
            errorMessage = "Please enter your email."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = mapOf("email" to email)
                val response = RetrofitClient.api.forgotPassword(request)

                if (response.isSuccessful) {
                    emailSentSuccess = true
                } else {
                    errorMessage = "Failed to send code. Is the email correct?"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        if (code.length != 6) {
            errorMessage = "Code must be 6 digits."
            return
        }
        if (newPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = mapOf("email" to email, "code" to code, "newPassword" to newPassword)
                val response = RetrofitClient.api.resetPassword(request)

                if (response.isSuccessful) {
                    passwordResetSuccess = true
                } else {
                    errorMessage = "Failed: Invalid code or password is the same."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetState() {
        emailSentSuccess = false
        passwordResetSuccess = false
        errorMessage = null
    }
}