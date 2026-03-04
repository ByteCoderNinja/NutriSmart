package com.example.nutrismart.ui.screens.verify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.VerifyRequest
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.launch

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
                    UserSession.token = token
                    UserSession.currentUserId = authResponse.userId
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