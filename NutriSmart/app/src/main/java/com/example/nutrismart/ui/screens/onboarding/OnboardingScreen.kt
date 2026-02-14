package com.example.nutrismart.ui.screens.onboarding

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.data.model.*
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onProfileComplete: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showActivityMenu by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.birthDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    )

    val numberRegex = Regex("^\\d*\\.?\\d*$")

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
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Build Your Plan", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Tell AI about yourself", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("Personal Details")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Gender.entries.forEach { g ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (viewModel.gender == g),
                        onClick = { viewModel.gender = g }
                    )
                    Text(text = g.name)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = viewModel.birthDate.toString(),
                onValueChange = { },
                label = { Text("Date of Birth") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Box(modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Unit System:", fontWeight = FontWeight.SemiBold)
            Row {
                FilterChip(
                    selected = !viewModel.isImperial,
                    onClick = { viewModel.isImperial = false },
                    label = { Text("Metric") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = viewModel.isImperial,
                    onClick = { viewModel.isImperial = true },
                    label = { Text("Imperial") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.height,
                onValueChange = { if (it.matches(numberRegex)) viewModel.height = it },
                label = { Text(if (viewModel.isImperial) "Height (ft)" else "Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = viewModel.weight,
                onValueChange = { if (it.matches(numberRegex)) viewModel.weight = it },
                label = { Text(if (viewModel.isImperial) "Weight (lbs)" else "Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Goals & Lifestyle")

        OutlinedTextField(
            value = viewModel.targetWeight,
            onValueChange = { if (it.matches(numberRegex)) viewModel.targetWeight = it },
            label = { Text(if (viewModel.isImperial) "Target Weight (lbs)" else "Target Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = showActivityMenu,
                onDismissRequest = { showActivityMenu = false }
            ) {
                ActivityLevel.entries.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.name) },
                        onClick = {
                            viewModel.activityLevel = level
                            showActivityMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Currency:", fontWeight = FontWeight.SemiBold)
            Row {
                FilterChip(
                    selected = viewModel.currency == Currency.RON,
                    onClick = { viewModel.currency = Currency.RON },
                    label = { Text("RON") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = viewModel.currency == Currency.EUR,
                    onClick = { viewModel.currency = Currency.EUR },
                    label = { Text("EUR") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = viewModel.currency == Currency.USD,
                    onClick = { viewModel.currency = Currency.USD },
                    label = { Text("USD") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = viewModel.currency == Currency.GBP,
                    onClick = { viewModel.currency = Currency.GBP },
                    label = { Text("GBP") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.budget,
            onValueChange = { if (it.matches(numberRegex)) viewModel.budget = it },
            label = { Text(
                when (viewModel.currency) {
                    Currency.EUR ->
                    "Daily Food Budget (EUR)"
                    Currency.RON ->
                    "Daily Food Budget (RON)"
                    Currency.USD ->
                    "Daily Food Budget (USD)"
                    else -> "Daily Food Budget (GBP)"
                }
            ) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Preferences & Health")

        MultiSelectDropdown(
            label = "Dietary Preferences",
            items = DietaryPreference.entries,
            selectedItems = viewModel.selectedDietaryPreferences,
            onSelectionChanged = { viewModel.toggleDiet(it) },
            itemLabel = { it.name }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MultiSelectDropdown(
            label = "Medical Conditions",
            items = MedicalCondition.entries,
            selectedItems = viewModel.selectedMedicalConditions,
            onSelectionChanged = { viewModel.toggleCondition(it) },
            itemLabel = { it.name }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.errorMessage != null) {
            Text(viewModel.errorMessage!!, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
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
                onClick = { viewModel.submitProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Generate My AI Plan")
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdown(
    label: String,
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChanged: (T) -> Unit,
    itemLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = if (selectedItems.isEmpty()) {
        "None selected"
    } else {
        selectedItems.joinToString(", ") { itemLabel(it) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(itemLabel(item))
                        }
                    },
                    onClick = {
                        onSelectionChanged(item)
                    }
                )
            }
        }
    }
}