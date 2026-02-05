package com.example.nutrismart.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigateToWeather: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Salutul de sus
        Text(text = "Salut, Alex!", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Hai să vedem progresul tău.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Grid-ul de carduri (Calories, Steps, etc.)
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(title = "Calories", value = "1200", icon = Icons.Default.LocalFireDepartment, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            InfoCard(title = "Steps", value = "5400", icon = Icons.Default.DirectionsWalk, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(title = "Water", value = "1.2L", icon = Icons.Default.WaterDrop, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            InfoCard(title = "Sleep", value = "7h 30m", icon = Icons.Default.Bedtime, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buton rapid spre Weather (conform fluxului)
        Button(onClick = onNavigateToWeather, modifier = Modifier.fillMaxWidth()) {
            Text("Vezi Recomandări Meteo")
        }
    }
}


@Composable
fun InfoCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = title)
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}