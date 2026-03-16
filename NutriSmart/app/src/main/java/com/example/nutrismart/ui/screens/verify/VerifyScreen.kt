package com.example.nutrismart.ui.screens.verify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VerifyScreen(
    email: String,
    viewModel: VerifyViewModel = viewModel(),
    onVerificationSuccess: () -> Unit
) {
    LaunchedEffect(viewModel.verificationSuccess) {
        if (viewModel.verificationSuccess) {
            onVerificationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Verify Email",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Code sent to: $email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.code,
            onValueChange = { viewModel.code = it },
            label = { Text("6-Digit Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        if (viewModel.successMessage != null) {
            Text(viewModel.successMessage!!, color = Color.Green, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.verify(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading && !viewModel.isResending
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(size = 20.dp, color = Color.White)
            } else {
                Text("Verify Code")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.resendCode(email) },
            enabled = !viewModel.isLoading && !viewModel.isResending
        ) {
            if (viewModel.isResending) {
                CircularProgressIndicator(size = 20.dp, color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Didn't receive a code? Resend")
            }
        }
    }
}

@Composable
fun CircularProgressIndicator(size: androidx.compose.ui.unit.Dp, color: Color) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}