package com.example.nutrismart.ui.screens.verify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.model.VerifyRequest
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.launch

object SessionManager {
    var token: String? = null
    var userId: Long? = null

    fun clearSession() {
        token = null
        userId = null
    }
}

class VerifyViewModel : ViewModel() {
    var code by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var verificationSuccess by mutableStateOf(false)

    fun verify(email: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val request = VerifyRequest(email, code)
                val response = RetrofitClient.api.verify(request)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    val authResponse = response.body()!!
                    SessionManager.token = "Bearer $token"
                    SessionManager.userId = authResponse.userId
                    verificationSuccess = true
                } else {
                    errorMessage = "Invalid code"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}