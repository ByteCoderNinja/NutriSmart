package com.example.nutrismart.ui.screens.onboarding.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutrismart.data.model.DietaryPreference
import com.example.nutrismart.data.model.MedicalCondition
import com.example.nutrismart.ui.core_components.DislikedFoodsSelector
import com.example.nutrismart.ui.screens.onboarding.OnboardingViewModel

@Composable
fun PreferencesHealthSection(
    viewModel: OnboardingViewModel,
    premiumShape: RoundedCornerShape,
    textFieldColors: TextFieldColors
) {
    SectionHeader("Foods to Avoid")
    Text(
        text = "Search ingredients you dislike or are allergic to:",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    DislikedFoodsSelector(
        searchQuery = viewModel.foodSearchQuery,
        onSearchQueryChange = viewModel::onFoodSearchQueryChanged,
        suggestions = viewModel.foodSuggestions,
        selectedFoods = viewModel.selectedDislikedFoods,
        onFoodToggled = { food ->
            viewModel.toggleDislikedFood(food)
        },
        isLoading = viewModel.isSearchingFoods
    )

    Spacer(modifier = Modifier.height(32.dp))

    SectionHeader("Preferences & Health")

    MultiSelectDropdownPremium(
        label = "Dietary Preferences",
        items = DietaryPreference.entries,
        selectedItems = viewModel.selectedDietaryPreferences,
        onSelectionChanged = { viewModel.toggleDiet(it) },
        itemLabel = { it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() } },
        colors = textFieldColors,
        shape = premiumShape
    )

    Spacer(modifier = Modifier.height(16.dp))

    MultiSelectDropdownPremium(
        label = "Medical Conditions",
        items = MedicalCondition.entries,
        selectedItems = viewModel.selectedMedicalConditions,
        onSelectionChanged = { viewModel.toggleCondition(it) },
        itemLabel = { it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() } },
        colors = textFieldColors,
        shape = premiumShape
    )
}
