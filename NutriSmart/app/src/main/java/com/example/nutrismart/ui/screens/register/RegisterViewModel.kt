package com.example.nutrismart.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.model.RegisterRequest
import com.example.nutrismart.data.repository.AuthRepository
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
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val request = RegisterRequest(email, password, username)
                val response = authRepository.register(request)
                if (response.isSuccessful) {
                    navigateToVerify = true
                } else {
                    errorMessage = "Registration failed. Email might be used."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
