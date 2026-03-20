package com.example.nutrismart.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.ui.screens.onboarding.components.*
import java.time.Instant
import java.time.ZoneId

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileComplete: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = key(viewModel.birthDate) {
        rememberDatePickerState(
            initialSelectedDateMillis = viewModel.birthDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
    }

    LaunchedEffect(Unit) {
        if (isEditMode) {
            viewModel.loadUserData()
        }
    }

    LaunchedEffect(viewModel.isComplete) {
        if (viewModel.isComplete) {
            onProfileComplete()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.birthDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1E24) else Color(0xFFF1F4F1)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = containerColor,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        disabledTextColor = MaterialTheme.colorScheme.onSurface
    )
    val premiumShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (isEditMode) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isEditMode) "Edit Your Plan" else "Build Your Plan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isEditMode) "Update your details for AI" else "Tell AI about yourself",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        PersonalDetailsSection(
            viewModel = viewModel,
            containerColor = containerColor,
            premiumShape = premiumShape,
            textFieldColors = textFieldColors,
            onShowDatePicker = { showDatePicker = true }
        )

        GoalsLifestyleSection(
            viewModel = viewModel,
            containerColor = containerColor,
            premiumShape = premiumShape,
            textFieldColors = textFieldColors
        )

        PreferencesHealthSection(
            viewModel = viewModel,
            premiumShape = premiumShape,
            textFieldColors = textFieldColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.loadingMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                enabled = !viewModel.isLoading,
                onClick = { viewModel.submitProfile() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Generate My AI Plan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}
