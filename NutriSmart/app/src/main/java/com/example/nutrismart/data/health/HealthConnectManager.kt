package com.example.nutrismart.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun readTodaySteps(): Int {
        if (!isAvailable) return 0

        return try {
            val startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)

            val endOfDay = startOfDay.plusDays(1)

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )

            (response[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()

        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}