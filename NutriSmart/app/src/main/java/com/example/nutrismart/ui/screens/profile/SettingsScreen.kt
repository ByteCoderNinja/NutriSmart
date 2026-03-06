package com.example.nutrismart.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.SessionManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.example.nutrismart.ui.screens.profile.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToEditPlan: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var wakeUpTime by remember {
        mutableStateOf(LocalTime.parse(sessionManager.getWakeUpTime()))
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = wakeUpTime.hour,
        initialMinute = wakeUpTime.minute,
        is24Hour = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsItem(
                title = "Wake Up Time",
                value = wakeUpTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onClick = { showTimePicker = true }
            )

            SettingsItem("Edit account", onClick = onNavigateToEditAccount)
            SettingsItem("Edit plan", onClick = onNavigateToEditPlan)
            SettingsItem("Logout", onClick = {
                viewModel.logout(onSuccess = onNavigateToLogin)
                sessionManager.clearSession()
                UserSession.clear()
            })
            SettingsItem(
                title = "Delete account",
                textColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteDialog = true }
            )
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    wakeUpTime = newTime
                    sessionManager.saveWakeUpTime(newTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(
                            onSuccess = onNavigateToLogin,
                            onError = {
                                Toast.makeText(context, "Delete Account Error!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}