package com.example.nutrismart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Asigură-te că importurile acestea corespund cu pachetele tale reale.
// Dacă ai pus ecranele în 'ui.screens', modifică aici 'ui.navigation' cu 'ui.screens'
import com.example.nutrismart.ui.navigation.HomeScreen
import com.example.nutrismart.ui.navigation.LoginScreen
import com.example.nutrismart.ui.navigation.RegisterScreen
import com.example.nutrismart.ui.navigation.Screen
import com.example.nutrismart.ui.theme.NutriSmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Activează afișarea pe tot ecranul
        setContent {
            NutriSmartTheme {
                // Aici apelăm funcția principală care conține NavHost-ul
                // Folosim Surface pentru a avea fundalul corect al temei
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NutriSmartApp()
                }
            }
        }
    }
}

@Composable
fun NutriSmartApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Ruta 1: Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    // Navigăm spre Home și ștergem Login din istoric (ca să nu dai Back la Login)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Ruta 2: Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // După înregistrare mergem la Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Ruta 3: Home
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWeather = { navController.navigate(Screen.Weather.route) }
            )
        }

        // Ruta 4: Weather (Placeholder momentan)
        composable(Screen.Weather.route) {
            // Doar un text temporar până facem ecranul Weather
            Surface(modifier = Modifier.fillMaxSize()) {
                Text(text = "Aici va fi ecranul Weather")
            }
        }
    }
}