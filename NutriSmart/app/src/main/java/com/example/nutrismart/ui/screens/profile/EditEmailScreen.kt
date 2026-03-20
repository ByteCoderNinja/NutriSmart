package com.example.nutrismart.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmailScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isGoogleUser) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Email cannot be changed for Google accounts", Toast.LENGTH_LONG).show()
            onBackClick()
        }
        return
    }

    var newEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.user) {
        uiState.user?.email?.let {
            if (newEmail.isEmpty()) {
                newEmail = it
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Email") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            if (uiState.isLoading && uiState.user == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("New Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (newEmail.isNotBlank() && newEmail != uiState.user?.email) {
                        isLoading = true
                        viewModel.updateEmail(
                            newEmail = newEmail,
                            onSuccess = {
                                isLoading = false
                                onNavigateToVerify(newEmail)
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && newEmail.isNotBlank() && newEmail != uiState.user?.email
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Verify & Save")
                }
            }
        }
    }
}
