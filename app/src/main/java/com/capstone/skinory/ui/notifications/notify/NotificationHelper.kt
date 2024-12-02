package com.capstone.skinory.ui.notifications.notify

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class NotificationHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun scheduleRoutineReminder(isDay: Boolean) {
        Log.d("AlarmManager", "Scheduling ${if (isDay) "morning" else "evening"} routine reminder")
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("is_day", isDay)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            if (isDay) DAY_ROUTINE_REQUEST_CODE else NIGHT_ROUTINE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Atur waktu notifikasi
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, if (isDay) 6 else 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Jika waktu yang dipilih sudah lewat, tambahkan satu hari
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        Log.d("AlarmManager", "Alarm set for: ${calendar.time}")

        // Set alarm berulang setiap hari
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d("AlarmManager", "Current time: ${Calendar.getInstance().time}")
        Log.d("AlarmManager", "Scheduled time: ${calendar.time}")
    }

    fun cancelRoutineReminder(isDay: Boolean) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            if (isDay) DAY_ROUTINE_REQUEST_CODE else NIGHT_ROUTINE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val DAY_ROUTINE_REQUEST_CODE = 100
        private const val NIGHT_ROUTINE_REQUEST_CODE = 101
    }
}