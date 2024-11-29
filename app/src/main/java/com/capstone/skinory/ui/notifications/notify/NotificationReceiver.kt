package com.capstone.skinory.ui.notifications.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.capstone.skinory.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isDay = intent.getBooleanExtra("is_day", true)

        // Buat saluran notifikasi (untuk Android Oreo ke atas)
        createNotificationChannel(context)

        // Buat dan tampilkan notifikasi
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentTitle("Skincare Routine Reminder")
            .setContentText(if (isDay) "Time for your morning skincare routine!" else "Time for your evening skincare routine!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(if (isDay) DAY_ROUTINE_REQUEST_CODE else NIGHT_ROUTINE_REQUEST_CODE, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Skincare Routine Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "skincare_routine_channel"
        private const val DAY_ROUTINE_REQUEST_CODE = 100
        private const val NIGHT_ROUTINE_REQUEST_CODE = 101
    }
}