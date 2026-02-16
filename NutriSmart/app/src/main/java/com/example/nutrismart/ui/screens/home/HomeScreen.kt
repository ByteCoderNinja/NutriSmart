package com.example.nutrismart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    var waterConsumedMl by remember { mutableIntStateOf(500) }
    val glassSizeMl = 250

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            DateHeaderSection()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            MainStatsCard(
                waterConsumedMl = waterConsumedMl,
                glassSizeMl = glassSizeMl,
                onAddWaterClick = { waterConsumedMl += glassSizeMl }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            MacrosRow()
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Today's Meal Plan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MealCardButton(
                mealType = "Breakfast",
                description = "Oatmeal with berries & nuts",
                onCardClick = {},
                onAddClick = {}
            )
            MealCardButton(
                mealType = "Lunch",
                description = "Grilled chicken salad",
                onCardClick = {},
                onAddClick = {}
            )
            MealCardButton(
                mealType = "Dinner",
                description = "Salmon with asparagus",
                onCardClick = {},
                onAddClick = {}
            )
            MealCardButton(
                mealType = "Snack",
                description = "Apple & peanut butter",
                onCardClick = {},
                onAddClick = {}
            )
        }
    }
}

@Composable
fun DateHeaderSection() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MainStatsCard(
    waterConsumedMl: Int,
    glassSizeMl: Int,
    onAddWaterClick: () -> Unit
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
            Text("1500", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Steps", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("4,520 / 10,000", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            WaterTrackerSection(waterConsumedMl, glassSizeMl, onAddWaterClick)
        }
    }
}

@Composable
fun WaterTrackerSection(consumedMl: Int, glassSize: Int, onAddClick: () -> Unit) {
    val consumedGlassesCount = consumedMl / glassSize
    val waterColor = Color(0xFF2196F3)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Water Intake", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text("$consumedMl ml", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(consumedGlassesCount) {
                Icon(
                    imageVector = Icons.Filled.LocalDrink,
                    contentDescription = "Consumed glass",
                    tint = waterColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalDrink,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add water",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MacrosRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroCard(modifier = Modifier.weight(1f), title = "Carbs", value = "120g", progress = 0.6f, color = Color(0xFFFFA726))
        MacroCard(modifier = Modifier.weight(1f), title = "Protein", value = "90g", progress = 0.8f, color = Color(0xFFEF5350))
        MacroCard(modifier = Modifier.weight(1f), title = "Fat", value = "45g", progress = 0.4f, color = Color(0xFF66BB6A))
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

@Composable
fun MealCardButton(
    mealType: String,
    description: String,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mealType, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }

            FilledTonalIconButton(
                onClick = { onAddClick() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log $mealType"
                )
            }
        }
    }
}