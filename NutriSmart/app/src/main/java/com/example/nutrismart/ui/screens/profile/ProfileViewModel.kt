package com.example.nutrismart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.UserDto
import com.example.nutrismart.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = UserSession.currentUserId
                val token = "Bearer ${UserSession.token}"

                val response = RetrofitClient.api.getUser(token, userId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(user = response.body(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Failed to load data", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = UserSession.currentUserId
                val token = "Bearer ${UserSession.token}"

                val response = RetrofitClient.api.deleteUser(token, userId)
                if (response.isSuccessful) {
                    UserSession.clear()
                    onSuccess()
                } else {
                    onError()
                }
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        UserSession.clear()
        onSuccess()
    }

    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${UserSession.token}"

                val request = com.example.nutrismart.data.model.UpdateUserRequest(username = newUsername)

                val response = RetrofitClient.api.patchUser(
                    token = token,
                    userId = UserSession.currentUserId,
                    request = request
                )

                if (response.isSuccessful) {
                    _uiState.update { it.copy(user = response.body()) }
                    onSuccess()
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    onError("Error $errorCode: $errorBody")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateEmail(newEmail: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${UserSession.token}"
                val request = com.example.nutrismart.data.model.UpdateUserRequest(email = newEmail)

                val response = RetrofitClient.api.patchUser(
                    token,
                    UserSession.currentUserId,
                    request
                )

                if (response.isSuccessful) {
                    _uiState.update { it.copy(user = response.body()) }
                    onSuccess()
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    onError("Error $errorCode: $errorBody")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updatePassword(currentPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = "Bearer ${UserSession.token}"
                val request = com.example.nutrismart.data.model.UpdateUserRequest(
                    currentPassword = currentPass,
                    newPassword = newPass
                )

                val response = RetrofitClient.api.patchUser(token, UserSession.currentUserId, request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    onError("Error $errorCode: $errorBody")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}