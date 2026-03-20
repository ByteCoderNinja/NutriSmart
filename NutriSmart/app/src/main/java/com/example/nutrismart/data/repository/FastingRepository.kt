package com.example.nutrismart.data.repository

import com.example.nutrismart.data.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FastingRepository @Inject constructor(
    private val sessionManager: SessionManager
) {
    fun isFastingActive(): Boolean = sessionManager.isFastingActive()
    
    fun getFastingEndTime(): Long = sessionManager.getFastingEndTime()
    
    fun getFastingStartTime(): Long = sessionManager.getFastingStartTime()
    
    fun getFastingDurationHours(): Int = sessionManager.getFastingDurationHours()
    
    fun clearFastingState() = sessionManager.clearFastingState()
    
    fun saveFastingState(isActive: Boolean, startTime: Long, endTime: Long, durationHours: Int) {
        sessionManager.saveFastingState(isActive, startTime, endTime, durationHours)
    }
}
