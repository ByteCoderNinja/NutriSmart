package com.example.nutrismart.ui.core_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrismart.data.model.DislikedFood

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DislikedFoodsSelector(
    selectedFoods: Set<DislikedFood>,
    onFoodToggled: (DislikedFood, Boolean) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DislikedFood.entries.forEach { food ->
            FilterChip(
                selected = selectedFoods.contains(food),
                onClick = {
                    val isCurrentlySelected = selectedFoods.contains(food)
                    onFoodToggled(food, !isCurrentlySelected)
                },
                label = { Text(food.displayName) }
            )
        }
    }
}