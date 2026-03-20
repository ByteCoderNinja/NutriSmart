package com.example.nutrismart.ui.screens.profile

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val VividRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToEditPlan: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var wakeUpTime by remember { 
        mutableStateOf(LocalTime.parse(viewModel.getWakeUpTime())) 
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you absolutely sure? This action is permanent and all your AI-generated plans and progress will be lost forever.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(
                            onSuccess = onNavigateToLogin,
                            onError = { Toast.makeText(context, "Account deletion error!", Toast.LENGTH_SHORT).show() }
                        )
                    }
                ) {
                    Text("Delete Forever", color = VividRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newTime = LocalTime.of(hourOfDay, minute)
            wakeUpTime = newTime
            viewModel.saveWakeUpTime(newTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        },
        wakeUpTime.hour,
        wakeUpTime.minute,
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Account Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )

            SettingsItem(
                title = "Edit Account Information",
                subtitle = "Change username, email or password",
                icon = Icons.Default.Person,
                onClick = onNavigateToEditAccount
            )

            SettingsItem(
                title = "Edit Personal Plan",
                subtitle = "Update physical data or goals",
                icon = Icons.Default.Settings,
                onClick = onNavigateToEditPlan
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "App Preferences",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { timePickerDialog.show() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    val formattedTime = wakeUpTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    Text("Wake-up Time", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Currently: $formattedTime (used for reminders)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout {
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividRed)
            ) {
                Text("Logout", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Account", color = VividRed, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsItem(
    title: String, 
    subtitle: String? = null, 
    icon: ImageVector, 
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
