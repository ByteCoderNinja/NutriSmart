package com.example.nutrismart.notifications

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class EatingReminder : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val mealType = intent?.getStringExtra("MEAL_TYPE") ?: "Meal"
        val isReminder = intent?.getBooleanExtra("IS_REMINDER", false) ?: false

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "eating_channel"

        val channel = NotificationChannel(
            channelId,
            "Meal Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val title = if (isReminder) "Still hungry?" else "Meal Time!"
        val message = if (isReminder)
            "Eat your $mealType! Or check it if you've already eaten it!"
        else "It's time for your $mealType."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(mealType.hashCode() + (if(isReminder) 1 else 0), notification)
    }
}