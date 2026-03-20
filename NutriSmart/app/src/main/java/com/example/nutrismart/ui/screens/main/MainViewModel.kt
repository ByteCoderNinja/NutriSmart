package com.example.nutrismart.ui.screens.main

import androidx.lifecycle.ViewModel
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun getStartDestination(): String {
        val token = authRepository.getAuthToken()
        val userId = authRepository.getUserId()
        val isVerified = authRepository.isVerified()
        val isProfileComplete = authRepository.isProfileComplete()

        if (!token.isNullOrEmpty()) {
            UserSession.token = token
            UserSession.currentUserId = userId
        }

        return when {
            token.isNullOrEmpty() -> "login"
            !isVerified -> "verify_initial"
            !isProfileComplete -> "onboarding"
            else -> "main_screen"
        }
    }
}
