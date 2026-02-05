package com.example.nutrismart.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Weather : Screen("weather")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Testing : Screen("testing")
}