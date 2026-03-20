package com.example.nutrismart.ui.screens.verify

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VerifyScreen(
    email: String,
    onVerificationSuccess: () -> Unit,
    viewModel: VerifyViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.verificationSuccess) {
        if (viewModel.verificationSuccess) {
            onVerificationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Email Verification", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter the 6-digit code sent to $email", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.code,
            onValueChange = { if (it.length <= 6) viewModel.code = it },
            label = { Text("6-Digit Code") },
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        if (viewModel.successMessage != null) {
            Text(viewModel.successMessage!!, color = Color(0xFF4CAF50), modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.verify(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading && viewModel.code.length == 6
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Verify Email")
            }
        }

        TextButton(
            onClick = { viewModel.resendCode(email) },
            enabled = !viewModel.isResending
        ) {
            if (viewModel.isResending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Resend Verification Code")
            }
        }
    }
}
