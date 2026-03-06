package com.example.nutrismart.ui.screens.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrismart.data.model.Gender
import com.example.nutrismart.ui.screens.onboarding.OnboardingViewModel

@Composable
fun PersonalDetailsSection(
    viewModel: OnboardingViewModel,
    containerColor: Color,
    premiumShape: RoundedCornerShape,
    textFieldColors: TextFieldColors,
    onShowDatePicker: () -> Unit
) {
    val numberRegex = Regex("^\\d*\\.?\\d*$")

    SectionHeader("Personal Details")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Gender.entries.forEach { g ->
            val isSelected = viewModel.gender == g
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(premiumShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else containerColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = premiumShape
                    )
                    .clickable { viewModel.gender = g }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = g.name,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.birthDate.toString(),
            onValueChange = { },
            label = { Text("Date of Birth") },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            shape = premiumShape,
            colors = textFieldColors
        )
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { onShowDatePicker() }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    PremiumSelectionRow(
        title = "Unit System",
        options = listOf("Metric", "Imperial"),
        selectedIndex = if (viewModel.isImperial) 1 else 0,
        onSelect = { viewModel.isImperial = it == 1 },
        containerColor = containerColor,
        shape = premiumShape
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = viewModel.height,
            onValueChange = { if (it.matches(numberRegex)) viewModel.height = it },
            label = { Text(if (viewModel.isImperial) "Height (ft)" else "Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = premiumShape,
            colors = textFieldColors,
            singleLine = true
        )
        OutlinedTextField(
            value = viewModel.weight,
            onValueChange = { if (it.matches(numberRegex)) viewModel.weight = it },
            label = { Text(if (viewModel.isImperial) "Weight (lbs)" else "Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = premiumShape,
            colors = textFieldColors,
            singleLine = true
        )
    }
}
