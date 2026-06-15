package com.example.nutrismart.ui.screens.register

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.model.RegisterRequest
import com.example.nutrismart.data.repository.AuthRepository
import com.example.nutrismart.data.remote.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var navigateToVerify by mutableStateOf(false)

    fun register() {
        if (!isEmailValid(email)) {
            errorMessage = "Please enter a valid email address."
            return
        }

        if (!isPasswordStrong(password)) {
            errorMessage = "Password must be at least 6 chars long and contain letters & numbers."
            return
        }

        if (!isUsernameValid(username)) {
            errorMessage = "Username cannot be empty and it has to be 3 characters at least."
            return
        }

        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val request = RegisterRequest(email, password, username)
                val response = authRepository.register(request)
                if (response.isSuccessful) {
                    navigateToVerify = true
                } else {
                    errorMessage = response.getErrorMessage("Registration failed. Email might be used.")
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun isUsernameValid(username: String): Boolean {
        return username.length >= 3
    }

    fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 6 && hasLetter && hasDigit
    }

    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
