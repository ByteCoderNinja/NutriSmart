package com.example.nutrismart.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrismart.ui.screens.profile.components.SettingsItem

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
                title = { Text("Edit Account") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            SettingsItem("Change Username", onClick = onEditUsernameClick)

            if (!uiState.isGoogleUser) {
                SettingsItem("Change Email", onClick = onEditEmailClick)
                SettingsItem("Change Password", onClick = onEditPasswordClick)
            } else {
                Text(
                    text = "Account managed by Google",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
