package com.example.nutrismart.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountSelectionScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onEditUsernameClick: () -> Unit,
    onEditEmailClick: () -> Unit,
    onEditPasswordClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(
                "Personal Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )

            SettingsItem(
                title = "Change Username",
                subtitle = "Current: ${uiState.user?.username ?: "..."}",
                icon = Icons.Default.Person,
                onClick = onEditUsernameClick
            )

            if (!uiState.isGoogleUser) {
                SettingsItem(
                    title = "Change Email",
                    subtitle = "Current: ${uiState.user?.email ?: "..."}",
                    icon = Icons.Default.Email,
                    onClick = onEditEmailClick
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Security",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )

                SettingsItem(
                    title = "Change Password",
                    subtitle = "Update your login credentials",
                    icon = Icons.Default.Lock,
                    onClick = onEditPasswordClick
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "This account is linked with Google. Email and Password management is handled by Google.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
