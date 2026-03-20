package com.example.nutrismart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.model.UserDto
import com.example.nutrismart.data.repository.AuthRepository
import com.example.nutrismart.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val isGoogleUser: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val token: String get() = UserSession.token ?: ""
    private val userId: Long get() = UserSession.currentUserId

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = userRepository.getUser(token, userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    _uiState.update { it.copy(
                        user = user,
                        isGoogleUser = user?.isGoogleUser ?: false,
                        isLoading = false
                    ) }
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
                val response = userRepository.deleteUser(token, userId)
                if (response.isSuccessful) {
                    authRepository.clearSession()
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
        authRepository.clearSession()
        onSuccess()
    }

    fun getWakeUpTime(): String {
        return authRepository.getWakeUpTime()
    }

    fun saveWakeUpTime(time: String) {
        authRepository.saveWakeUpTime(time)
    }

    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.example.nutrismart.data.model.UpdateUserRequest(username = newUsername)
                val response = userRepository.patchUser(token, userId, request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(user = response.body()) }
                    onSuccess()
                } else {
                    onError("Error ${response.code()}: ${response.errorBody()?.string() ?: "Server error"}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun updateEmail(newEmail: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.example.nutrismart.data.model.UpdateUserRequest(email = newEmail)
                val response = userRepository.patchUser(token, userId, request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(user = response.body()) }
                    onSuccess()
                } else {
                    onError("Error ${response.code()}: ${response.errorBody()?.string() ?: "Server error"}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun updatePassword(currentPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.example.nutrismart.data.model.UpdateUserRequest(
                    currentPassword = currentPass,
                    newPassword = newPass
                )
                val response = userRepository.patchUser(token, userId, request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error ${response.code()}: ${response.errorBody()?.string() ?: "Server error"}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun isGoogleUser(): Boolean {
        return _uiState.value.isGoogleUser
    }
}
