package com.example.nutrismart.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.data.model.*
import com.example.nutrismart.ui.core_components.DislikedFoodsSelector
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    isEditMode: Boolean = false,
    onBackClick: () -> Unit = {},
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
                .clickable { showDatePicker = true }
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

        SectionHeader("Preferences & Health")

        MultiSelectDropdownPremium(
            label = "Dietary Preferences",
            items = DietaryPreference.entries,
            selectedItems = viewModel.selectedDietaryPreferences,
            onSelectionChanged = { viewModel.toggleDiet(it) },
            itemLabel = { it.name },
            colors = textFieldColors,
            shape = premiumShape
        )

        Spacer(modifier = Modifier.height(16.dp))

        MultiSelectDropdownPremium(
            label = "Medical Conditions",
            items = MedicalCondition.entries,
            selectedItems = viewModel.selectedMedicalConditions,
            onSelectionChanged = { viewModel.toggleCondition(it) },
            itemLabel = { it.name },
            colors = textFieldColors,
            shape = premiumShape
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("Foods to Avoid")
        Text("Select ingredients you dislike or are allergic to:", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

        DislikedFoodsSelector(
            selectedFoods = viewModel.selectedDislikedFoods,
            onFoodToggled = { food, isSelected ->
                viewModel.toggleDislikedFood(food, isSelected)
            }
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


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)
    )
}

@Composable
fun PremiumSelectionRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    containerColor: Color,
    shape: RoundedCornerShape,
    scrollable: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(16.dp))

        val baseModifier = Modifier.weight(1f)
        val rowModifier = if (scrollable) {
            baseModifier.horizontalScroll(rememberScrollState())
        } else {
            baseModifier
        }

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex

                val boxModifier = if (!scrollable) Modifier.weight(1f) else Modifier

                Box(
                    modifier = boxModifier
                        .clip(shape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else containerColor)
                        .clickable { onSelect(index) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdownPremium(
    label: String,
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChanged: (T) -> Unit,
    itemLabel: (T) -> String,
    colors: TextFieldColors,
    shape: RoundedCornerShape
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selectedItems.isEmpty()) "None selected" else selectedItems.joinToString(", ") { itemLabel(it) }

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
            colors = colors,
            shape = shape,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(itemLabel(item), color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    onClick = { onSelectionChanged(item) }
                )
            }
        }
    }
}