package com.example.nutrismart.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nutrismart_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit { putString("USER_TOKEN", token) }
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("USER_TOKEN", null)
    }

    fun saveUserId(userId: Long) {
        prefs.edit { putLong("USER_ID", userId) }
    }

    fun fetchUserId(): Long {
        return prefs.getLong("USER_ID", -1L)
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    fun saveFastingState(isFasting: Boolean, startTime: Long, endTime: Long, durationHours: Int) {
        prefs.edit {
            putBoolean("FASTING_IS_ACTIVE", isFasting)
                .putLong("FASTING_START_TIME", startTime)
                .putLong("FASTING_END_TIME", endTime)
                .putInt("FASTING_DURATION_HOURS", durationHours)
        }
    }

    fun isFastingActive(): Boolean = prefs.getBoolean("FASTING_IS_ACTIVE", false)
    fun getFastingStartTime(): Long = prefs.getLong("FASTING_START_TIME", 0L)
    fun getFastingEndTime(): Long = prefs.getLong("FASTING_END_TIME", 0L)
    fun getFastingDurationHours(): Int = prefs.getInt("FASTING_DURATION_HOURS", 16)

    fun clearFastingState() {
        prefs.edit {
            remove("FASTING_IS_ACTIVE")
                .remove("FASTING_START_TIME")
                .remove("FASTING_END_TIME")
        }
    }

    fun saveWaterIntake(ml: Int) {
        val today = java.time.LocalDate.now().toString()
        prefs.edit {
            putInt("WATER_CONSUMED", ml)
            putString("WATER_DATE", today)
        }
    }

    fun getWaterIntake(): Int {
        val savedDate = prefs.getString("WATER_DATE", "")
        val today = java.time.LocalDate.now().toString()
        return if (savedDate == today) {
            prefs.getInt("WATER_CONSUMED", 0)
        } else {
            0
        }
    }

    fun saveWakeUpTime(time: String) {
        prefs.edit { putString("wake_up_time", time) }
    }

    fun getWakeUpTime(): String {
        return prefs.getString("wake_up_time", "08:00") ?: "08:00"
    }

    fun saveProfileComplete(isComplete: Boolean) {
        prefs.edit { putBoolean("IS_PROFILE_COMPLETE", isComplete) }
    }

    fun isProfileComplete(): Boolean {
        return prefs.getBoolean("IS_PROFILE_COMPLETE", false)
    }

    fun saveIsVerified(isVerified: Boolean) {
        prefs.edit { putBoolean("IS_VERIFIED", isVerified) }
    }

    fun isVerified(): Boolean {
        return prefs.getBoolean("IS_VERIFIED", false)
    }

    fun saveIsGoogleUser(isGoogle: Boolean) {
        prefs.edit { putBoolean("IS_GOOGLE_USER", isGoogle) }
    }

    fun isGoogleUser(): Boolean {
        return prefs.getBoolean("IS_GOOGLE_USER", false)
    }
}