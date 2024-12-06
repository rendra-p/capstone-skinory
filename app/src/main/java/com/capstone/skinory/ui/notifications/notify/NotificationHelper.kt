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
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

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
                ).apply {
                    description = "Skincare Routine Notifications"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 300, 400)
                },
                NotificationChannel(
                    NIGHT_CHANNEL_ID,
                    "Night Skincare Routine",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Skincare Routine Notifications"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 300, 400)
                }
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun scheduleNotifications() {
        // Batalkan pekerjaan yang sudah ada
        cancelNotifications()

        // Schedule Day Notification at 6 AM
        scheduleRoutineNotification(
            requestCode = DAY_NOTIFICATION_REQUEST_CODE,
            hour = 6,
            minute = 0,
            type = "Day"
        )

        // Schedule Night Notification at 8 PM
        scheduleRoutineNotification(
            requestCode = NIGHT_NOTIFICATION_REQUEST_CODE,
            hour = 20,
            minute = 0,
            type = "Night"
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleRoutineNotification(requestCode: Int, hour: Int, minute: Int, type: String) {

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        // Buat constraints untuk pekerjaan
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Hitung delay yang tepat
        val delay = calendar.timeInMillis - System.currentTimeMillis()

        // Buat input data
        val inputData = Data.Builder()
            .putString("type", type)
            .putLong("scheduledTime", calendar.timeInMillis)
            .build()

        // Buat OneTimeWorkRequest dengan delay yang tepat
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(type)
            .build()

        // Batalkan pekerjaan sebelumnya dengan tipe yang sama
        workManager.cancelAllWorkByTag(type)

        // Jadwalkan pekerjaan baru
        workManager.enqueueUniqueWork(
            when (type) {
                "Day" -> NotificationWorker.WORK_NAME_DAY
                "Night" -> NotificationWorker.WORK_NAME_NIGHT
                else -> "unknown_routine"
            },
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        // Log detail penjadwalan
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d(TAG, "Scheduling $type Notification:")
        Log.d(TAG, "Scheduled Time: ${sdf.format(calendar.time)}")
        Log.d(TAG, "Current Time: ${sdf.format(System.currentTimeMillis())}")
        Log.d(TAG, "Delay: $delay ms")

        // Additional logging for verification
        Log.d(TAG, "$type Routine Notification scheduled for: ${calendar.time}")
    }

    fun cancelNotifications() {
        // Detailed logging for cancellation
        Log.d(TAG, "Attempting to cancel all scheduled notifications")

        // Batalkan notifikasi yang sudah dijadwalkan
        workManager.cancelAllWorkByTag("Day")
        workManager.cancelAllWorkByTag("Night")

        Log.d(TAG, "All notification works cancelled")
    }

    companion object {
        const val DAY_CHANNEL_ID = "DaySkinCareRoutineChannel"
        const val NIGHT_CHANNEL_ID = "NightSkinCareRoutineChannel"
        const val DAY_NOTIFICATION_REQUEST_CODE = 1
        const val NIGHT_NOTIFICATION_REQUEST_CODE = 2
        private const val TAG = "NotificationHelper"
    }
}