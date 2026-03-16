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
    var isResending by mutableStateOf(false)
    var resendSuccess by mutableStateOf(false)

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 6 && hasLetter && hasDigit
    }

    fun sendForgotPasswordEmail(email: String) {
        if (!isEmailValid(email)) {
            errorMessage = "Please enter a valid email address."
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
                    errorMessage = "Email not found or error sending code."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resendForgotPasswordEmail(email: String) {
        viewModelScope.launch {
            isResending = true
            errorMessage = null
            resendSuccess = false
            try {
                val request = mapOf("email" to email)
                val response = RetrofitClient.api.forgotPassword(request)
                if (response.isSuccessful) {
                    resendSuccess = true
                } else {
                    errorMessage = "Failed to resend code."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isResending = false
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        if (code.length != 6) {
            errorMessage = "Code must be 6 digits."
            return
        }
        if (!isPasswordStrong(newPassword)) {
            errorMessage = "Password must be at least 6 characters long and contain both letters and numbers."
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
                    errorMessage = "Invalid code or password match failed."
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