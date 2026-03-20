package com.example.nutrismart.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nutrismart.ui.screens.fasting.FastingScreen
import com.example.nutrismart.ui.screens.home.HomeScreen
import com.example.nutrismart.ui.screens.home.HomeViewModel
import com.example.nutrismart.ui.screens.profile.*
import com.example.nutrismart.ui.screens.weather.WeatherScreen

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home_tab", Icons.Default.Home, "Home")
    object Fasting : BottomNavItem("fasting_tab", Icons.Default.Timer, "Fasting")
    object Weather : BottomNavItem("weather_tab", Icons.Default.Cloud, "Weather")
    object Profile : BottomNavItem("profile_tab", Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToLogin: () -> Unit,
    onNavigateToEditPlan: () -> Unit,
    onNavigateToVerifyEmail: (String) -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Fasting,
        BottomNavItem.Weather,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = homeViewModel)
            }
            composable(BottomNavItem.Fasting.route) {
                FastingScreen()
            }
            composable(BottomNavItem.Weather.route) {
                // Weather might also need hiltViewModel later
                val homeViewModel: HomeViewModel = hiltViewModel()
                WeatherScreen(homeViewModel = homeViewModel)
            }
            composable(BottomNavItem.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate("settings_screen") }
                )
            }

            composable("settings_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToEditAccount = { navController.navigate("edit_account_screen") },
                    onNavigateToEditPlan = onNavigateToEditPlan,
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            composable("edit_account_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditAccountSelectionScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditUsernameClick = { navController.navigate("edit_username_screen") },
                    onEditEmailClick = { navController.navigate("edit_email_screen") },
                    onEditPasswordClick = { navController.navigate("edit_password_screen") }
                )
            }

            composable("edit_username_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditUsernameScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("edit_email_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditEmailScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToVerify = { email ->
                        onNavigateToVerifyEmail(email)
                    }
                )
            }

            composable("edit_password_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditPasswordScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToForgotPassword = {
                        profileViewModel.logout {
                            onNavigateToForgotPassword()
                        }
                    }
                )
            }
        }
    }
}
