package com.example.nutrismart.ui.screens.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nutrismart.notifications.EatingReminder

@SuppressLint("ScheduleExactAlarm")
fun scheduleMealNotification(
    context: Context,
    mealType: String,
    timeInMillis: Long,
    isReminder: Boolean
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EatingReminder::class.java).apply {
        putExtra("MEAL_TYPE", mealType)
        putExtra("IS_REMINDER", isReminder)
    }

    val requestCode = mealType.hashCode() + (if (isReminder) 1 else 0)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

fun cancelMealNotification(
    context: Context,
    mealType: String,
    isReminder: Boolean
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EatingReminder::class.java)
    val requestCode = mealType.hashCode() + (if (isReminder) 1 else 0)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        alarmManager.cancel(pendingIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
