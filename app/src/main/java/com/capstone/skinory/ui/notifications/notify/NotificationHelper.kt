package com.capstone.skinory.ui.notifications.notify

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    DAY_CHANNEL_ID,
                    "Day Skincare Routine",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    NIGHT_CHANNEL_ID,
                    "Night Skincare Routine",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )

            channels.forEach { channel ->
                channel.description = "Skincare Routine Notifications"
                channel.enableLights(true)
                channel.lightColor = Color.BLUE
                channel.enableVibration(true)

                notificationManager.createNotificationChannel(channel)
            }

            Log.d(TAG, "Notification channels created")
        }
    }

    fun scheduleNotifications() {
        // Schedule Day Notification at 6 AM
//        scheduleRoutineNotification(
//            requestCode = DAY_NOTIFICATION_REQUEST_CODE,
//            hour = 6,
//            minute = 0,
//            type = "Day"
//        )
//
//        // Schedule Night Notification at 8 PM
//        scheduleRoutineNotification(
//            requestCode = NIGHT_NOTIFICATION_REQUEST_CODE,
//            hour = 20,
//            minute = 0,
//            type = "Night"
//        )
        val currentTime = Calendar.getInstance()

        scheduleRoutineNotification(
            requestCode = DAY_NOTIFICATION_REQUEST_CODE,
            hour = currentTime.get(Calendar.HOUR_OF_DAY),
            minute = currentTime.get(Calendar.MINUTE) + 1, // Tambah 1 menit
            type = "Day"
        )
        scheduleRoutineNotification(
            requestCode = NIGHT_NOTIFICATION_REQUEST_CODE,
            hour = currentTime.get(Calendar.HOUR_OF_DAY),
            minute = currentTime.get(Calendar.MINUTE) + 2, // Tambah 2 menit
            type = "Night"
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleRoutineNotification(requestCode: Int, hour: Int, minute: Int, type: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.capstone.skinory.ROUTINE_NOTIFICATION"
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
//                add(Calendar.DAY_OF_YEAR, 1)
                add(Calendar.MINUTE, 1)
            }
        }

        // Log detailed scheduling information
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val scheduledTime = sdf.format(calendar.time)

        Log.d(TAG, "Final scheduling details:")
        Log.d(TAG, "Type: $type")
        Log.d(TAG, "Request Code: $requestCode")
        Log.d(TAG, "Scheduled Time: $scheduledTime")
        Log.d(TAG, "Current Time: ${sdf.format(Calendar.getInstance().time)}")

        // Gunakan metode alarm yang sesuai
        try {
            // Gunakan setAlarmClock untuk kompatibilitas maksimal
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )

            Log.d(TAG, "$type Routine Notification scheduled for: ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when scheduling notification", e)

            // Fallback method jika setAlarmClock gagal
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Fallback scheduling failed", ex)
            }
        }

        // Additional logging for verification
        Log.i(TAG, "$type Routine Notification scheduled successfully")
    }

    fun cancelNotifications() {
        // Detailed logging for cancellation
        Log.d(TAG, "Attempting to cancel all scheduled notifications")

        // Batalkan notifikasi yang sudah dijadwalkan
        val intents = listOf(
            Pair(DAY_NOTIFICATION_REQUEST_CODE, "Day"),
            Pair(NIGHT_NOTIFICATION_REQUEST_CODE, "Night")
        )

        intents.forEach { (requestCode, type) ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.capstone.skinory.ROUTINE_NOTIFICATION"
                putExtra("type", type)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "$type Notification cancelled")
        }
    }

    companion object {
        const val DAY_CHANNEL_ID = "DaySkinCareRoutineChannel"
        const val NIGHT_CHANNEL_ID = "NightSkinCareRoutineChannel"
        const val DAY_NOTIFICATION_REQUEST_CODE = 1
        const val NIGHT_NOTIFICATION_REQUEST_CODE = 2
        private const val TAG = "NotificationHelper"
    }
}