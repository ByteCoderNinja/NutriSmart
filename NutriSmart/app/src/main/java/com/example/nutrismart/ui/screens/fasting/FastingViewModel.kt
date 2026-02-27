package com.example.nutrismart.ui.screens.fasting

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrismart.data.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FastingState(
    val isFasting: Boolean = false,
    val selectedDurationHours: Int = 16,
    val endTimeInMillis: Long = 0L,
    val startTimeInMillis: Long = 0L,
    val progress: Float = 0f,
    val timeRemainingString: String = "00:00:00"
)

class FastingViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(FastingState())
    val uiState: StateFlow<FastingState> = _uiState.asStateFlow()

    init {
        restoreFastingState()
        startTimer()
    }

    private fun restoreFastingState() {
        val isActive = sessionManager.isFastingActive()
        if (isActive) {
            val endTime = sessionManager.getFastingEndTime()
            if (System.currentTimeMillis() >= endTime) {
                _uiState.update {
                    it.copy(
                        isFasting = false,
                        progress = 1f,
                        timeRemainingString = "00:00:00",
                        selectedDurationHours = sessionManager.getFastingDurationHours()
                    )
                }
                sessionManager.clearFastingState()
            } else {
                _uiState.update {
                    it.copy(
                        isFasting = true,
                        startTimeInMillis = sessionManager.getFastingStartTime(),
                        endTimeInMillis = endTime,
                        selectedDurationHours = sessionManager.getFastingDurationHours()
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(selectedDurationHours = sessionManager.getFastingDurationHours())
            }
        }
    }

    fun selectDuration(hours: Int) {
        if (!_uiState.value.isFasting) {
            _uiState.update { it.copy(selectedDurationHours = hours) }
        }
    }

    fun toggleFasting(context: Context) {
        val currentState = _uiState.value

        if (currentState.isFasting) {
            _uiState.update { it.copy(isFasting = false, progress = 0f, timeRemainingString = "00:00:00") }
            cancelNotification(context)
            sessionManager.clearFastingState()
        } else {
            val durationMillis = currentState.selectedDurationHours * 60 * 60 * 1000L
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMillis

            _uiState.update {
                it.copy(
                    isFasting = true,
                    startTimeInMillis = startTime,
                    endTimeInMillis = endTime
                )
            }
            scheduleNotification(context, endTime)
            sessionManager.saveFastingState(true, startTime, endTime, currentState.selectedDurationHours)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun startTimer() {
        viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                if (state.isFasting) {
                    val currentTime = System.currentTimeMillis()
                    val timeRemaining = state.endTimeInMillis - currentTime
                    val totalDuration = state.selectedDurationHours * 60 * 60 * 1000L

                    if (timeRemaining <= 0) {
                        _uiState.update { it.copy(isFasting = false, progress = 1f, timeRemainingString = "00:00:00") }
                    } else {
                        val progress = 1f - (timeRemaining.toFloat() / totalDuration.toFloat())

                        val hours = (timeRemaining / (1000 * 60 * 60)).toInt()
                        val mins = ((timeRemaining / (1000 * 60)) % 60).toInt()
                        val secs = ((timeRemaining / 1000) % 60).toInt()

                        val timeString = String.format("%02d:%02d:%02d", hours, mins, secs)

                        _uiState.update { it.copy(progress = progress, timeRemainingString = timeString) }
                    }
                }
                delay(1000)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FastingNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    private fun cancelNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FastingNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}