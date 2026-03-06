package com.example.nutrismart.ui.screens.home

import androidx.compose.runtime.Composable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun getRecommendedTimeRange(wakeUpTime: LocalTime, mealType: String): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    val (startOffset, endOffset) = when (mealType.uppercase()) {
        "BREAKFAST" -> Pair(1L, 2L)
        "LUNCH" -> Pair(5L, 6L)
        "SNACK" -> Pair(8L, 9L)
        "DINNER" -> Pair(10L, 11L)
        "BONUS SNACK" -> Pair(12L, 13L)
        else -> Pair(0L, 0L)
    }

    val startTime = wakeUpTime.plusHours(startOffset)
    val endTime = wakeUpTime.plusHours(endOffset)

    return "${startTime.format(formatter)} - ${endTime.format(formatter)}"
}
