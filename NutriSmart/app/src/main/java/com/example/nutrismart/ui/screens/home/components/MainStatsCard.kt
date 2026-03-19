package com.example.nutrismart.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrismart.ui.screens.home.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MainStatsCard(
    state: HomeUiState,
    hasPermissions: Boolean,
    isImperial: Boolean,
    isHealthConnectAvailable: Boolean = true,
    onAddWaterClick: () -> Unit,
    onRemoveWaterClick: () -> Unit,
    onStepsClick: () -> Unit,
    onAdjustWeight: (Double) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Calories Remaining", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${state.caloriesRemaining}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isHealthConnectAvailable) { onStepsClick() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Steps", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isHealthConnectAvailable) {
                        if (hasPermissions) {
                            Text("${state.steps} / ${state.stepsGoal}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Tap to connect Health", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Text("Health Connect not supported", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            WaterTrackerSection(
                state.waterConsumedMl,
                state.waterGoalMl,
                state.glassSizeMl,
                isImperial = isImperial,
                onAddWaterClick,
                onRemoveClick = onRemoveWaterClick
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            WeightTrackerSection(
                currentWeight = state.weight,
                isImperial = isImperial,
                onAdjustWeight = onAdjustWeight
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterTrackerSection(
    consumedMl: Int,
    goalMl: Int,
    glassSize: Int,
    isImperial: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val consumedGlassesCount = if (glassSize > 0) consumedMl / glassSize else 0
    val goalGlassesCount = if (glassSize > 0) goalMl / glassSize else 0
    val waterColor = Color(0xFF2196F3)

    val waterText = if (isImperial) {
        "$consumedGlassesCount / $goalGlassesCount cups"
    } else {
        "$consumedMl ml / $goalMl ml"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Water Intake", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(waterText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(consumedGlassesCount) {
                Icon(
                    imageVector = Icons.Filled.LocalDrink,
                    contentDescription = "Consumed glass",
                    tint = waterColor,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(48.dp)
                        .clickable { onRemoveClick() }
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(48.dp)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add water",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun WeightTrackerSection(
    currentWeight: Double,
    isImperial: Boolean,
    onAdjustWeight: (Double) -> Unit
) {
    val weightUnit = if (isImperial) "lbs" else "kg"
    val formattedWeight = String.format(Locale.US, "%.1f", currentWeight)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Weight", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            RepeatingIconButton(
                icon = Icons.Default.Remove,
                onClick = { onAdjustWeight(-0.1) }
            )

            Text(
                text = "$formattedWeight $weightUnit",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            RepeatingIconButton(
                icon = Icons.Default.Add,
                onClick = { onAdjustWeight(0.1) }
            )
        }
    }
}

@Composable
fun RepeatingIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onClick()

                        val job = coroutineScope.launch {
                            delay(400)
                            while (true) {
                                onClick()
                                delay(100)
                            }
                        }

                        tryAwaitRelease()
                        job.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}
