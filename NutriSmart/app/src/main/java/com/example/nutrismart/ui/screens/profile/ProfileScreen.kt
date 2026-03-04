package com.example.nutrismart.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.fetchUserData()
            isRefreshing = false
        }
    }

    fun calculateAge(birthDateString: String?): String {
        if (birthDateString.isNullOrEmpty()) return "--"
        return try {
            val dob = LocalDate.parse(birthDateString)
            val today = LocalDate.now()
            Period.between(dob, today).years.toString()
        } catch (e: Exception) { "--" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            if (uiState.isLoading && !isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.user?.username ?: "User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    ProfileInfoCard("Current Data", listOf(
                        "Email: ${uiState.user?.email ?: "N/A"}",
                        "Weight: ${uiState.user?.weight ?: "--"} kg",
                        "Height: ${uiState.user?.height ?: "--"} cm",
                        "Age: ${calculateAge(uiState.user?.dateOfBirth)} years"
                    ))

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfoCard("Goals", listOf(
                        "Target Weight: ${uiState.user?.targetWeight ?: "--"} kg",
                        "Daily Steps: ${uiState.user?.stepGoal ?: 10000}"
                    ))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            lines.forEach { line -> Text(line) }
        }
    }
}