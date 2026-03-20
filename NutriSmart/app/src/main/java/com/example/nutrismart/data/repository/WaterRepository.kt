package com.example.nutrismart.data.repository

import com.example.nutrismart.data.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepository @Inject constructor(
    private val sessionManager: SessionManager
) {
    fun getWaterIntake(): Int = sessionManager.getWaterIntake()

    fun saveWaterIntake(water: Int) {
        sessionManager.saveWaterIntake(water)
    }
}
