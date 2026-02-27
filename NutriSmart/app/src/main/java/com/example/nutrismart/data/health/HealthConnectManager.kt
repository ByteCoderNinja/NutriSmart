package com.example.nutrismart.data.health

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun readTodaySteps(): Int {
        if (!isAvailable) return 0

        return try {
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant()
            val now = Instant.now()

            val response = healthConnectClient.aggregate(
                androidx.health.connect.client.request.AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            (response[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()

        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}