package com.example.nutrismart.ui.screens.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrismart.data.model.ActivityLevel
import com.example.nutrismart.data.model.Currency
import com.example.nutrismart.ui.screens.onboarding.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsLifestyleSection(
    viewModel: OnboardingViewModel,
    containerColor: Color,
    premiumShape: RoundedCornerShape,
    textFieldColors: TextFieldColors
) {
    val numberRegex = Regex("^\\d*\\.?\\d*$")
    var showActivityMenu by remember { mutableStateOf(false) }

    SectionHeader("Goals & Lifestyle")

    OutlinedTextField(
        value = viewModel.targetWeight,
        onValueChange = { if (it.matches(numberRegex)) viewModel.targetWeight = it },
        label = { Text(if (viewModel.isImperial) "Target Weight (lbs)" else "Target Weight (kg)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = premiumShape,
        colors = textFieldColors,
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    ExposedDropdownMenuBox(
        expanded = showActivityMenu,
        onExpandedChange = { showActivityMenu = !showActivityMenu }
    ) {
        OutlinedTextField(
            value = viewModel.activityLevel.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Activity Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showActivityMenu) },
            colors = textFieldColors,
            shape = premiumShape,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = showActivityMenu,
            onDismissRequest = { showActivityMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            ActivityLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.name, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        viewModel.activityLevel = level
                        showActivityMenu = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    PremiumSelectionRow(
        title = "Currency",
        options = Currency.entries.map { it.name },
        selectedIndex = Currency.entries.indexOf(viewModel.currency),
        onSelect = { viewModel.currency = Currency.entries[it] },
        containerColor = containerColor,
        shape = premiumShape,
        scrollable = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = viewModel.budget,
        onValueChange = { if (it.matches(numberRegex)) viewModel.budget = it },
        label = { Text("Daily Food Budget (${viewModel.currency.name})") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = premiumShape,
        colors = textFieldColors,
        singleLine = true
    )
}
