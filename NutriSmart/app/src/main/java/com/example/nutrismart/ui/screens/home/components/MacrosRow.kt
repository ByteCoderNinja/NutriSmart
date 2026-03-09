package com.example.nutrismart.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrismart.ui.screens.home.HomeUiState

@Composable
fun MacrosRow(
    state: HomeUiState,
    isImperial: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val carbsProgress = if (state.carbsGoal > 0) state.carbsConsumed.toFloat() / state.carbsGoal else 0f
        val proteinProgress = if (state.proteinGoal > 0) state.proteinConsumed.toFloat() / state.proteinGoal else 0f
        val fatProgress = if (state.fatGoal > 0) state.fatConsumed.toFloat() / state.fatGoal else 0f

        MacroCard(modifier = Modifier.weight(1f), title = "Carbs", value = "${state.carbsConsumed}g", progress = carbsProgress, color = Color(0xFF2881B4))
        MacroCard(modifier = Modifier.weight(1f), title = "Protein", value = "${state.proteinConsumed}g", progress = proteinProgress, color = Color(0xFF5DB056))
        MacroCard(modifier = Modifier.weight(1f), title = "Fat", value = "${state.fatConsumed}g", progress = fatProgress, color = Color(0xFFFFA726))
    }
}

@Composable
fun MacroCard(modifier: Modifier = Modifier, title: String, value: String, progress: Float, color: Color) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}
