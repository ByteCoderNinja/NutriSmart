package com.example.nutrismart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.ui.screens.login.ForgotPasswordEmailScreen
import com.example.nutrismart.ui.screens.login.LoginScreen
import com.example.nutrismart.ui.screens.login.ResetPasswordScreen
import com.example.nutrismart.ui.screens.main.MainScreen
import com.example.nutrismart.ui.screens.onboarding.OnboardingScreen
import com.example.nutrismart.ui.screens.register.RegisterScreen
import com.example.nutrismart.ui.screens.verify.VerifyScreen

@Composable
fun NutriSmartApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val savedToken = sessionManager.fetchAuthToken()
    val savedUserId = sessionManager.fetchUserId()
    val isProfileComplete = sessionManager.isProfileComplete()

    if (!savedToken.isNullOrEmpty()) {
        UserSession.token = savedToken
        UserSession.currentUserId = savedUserId
    }

    val startDestination = when {
        savedToken.isNullOrEmpty() -> "login"
        isProfileComplete -> "main_screen"
        else -> "onboarding"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { profileComplete ->
                    val destination = if (profileComplete) "main_screen" else "onboarding"
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate("verify/$email")
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = "verify/{email}?isEdit={isEdit}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("isEdit") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val isEdit = backStackEntry.arguments?.getBoolean("isEdit") ?: false

            VerifyScreen(
                email = email,
                onVerificationSuccess = {
                    if (isEdit) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onProfileComplete = {
                    navController.navigate("main_screen") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("edit_plan") {
            OnboardingScreen(
                isEditMode = true,
                onBackClick = { navController.popBackStack() },
                onProfileComplete = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordEmailScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToVerify = { email ->
                    navController.navigate("reset_password/$email")
                }
            )
        }

        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordScreen(
                email = email,
                onBackClick = { navController.popBackStack() },
                onPasswordResetSuccess = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main_screen") {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToEditPlan = {
                    navController.navigate("edit_plan")
                },
                onNavigateToVerifyEmail = { email ->
                    navController.navigate("verify/$email?isEdit=true")
                }
            )
        }
    }
}