package com.capstone.skinory.ui.notifications.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.capstone.skinory.R
import com.capstone.skinory.data.UserPreferences
import java.util.Calendar
import kotlin.math.abs

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: return Result.failure()
        val scheduledTime = inputData.getLong("scheduledTime", 0)

        // Periksa apakah waktu sekarang sesuai dengan waktu yang dijadwalkan
        val currentTime = System.currentTimeMillis()
        val timeDifference = abs(currentTime - scheduledTime)

        // Toleransi 5 menit (300000 ms)
        if (timeDifference > 300000) {
            Log.d("NotificationWorker", "Skipping notification. Time mismatch.")
            return Result.success()
        }

        // Pastikan channel dibuat
        createNotificationChannel(applicationContext)

        // Tampilkan notifikasi
        showNotification(applicationContext, type)

        // Jadwalkan ulang notifikasi
        scheduleNextNotification(applicationContext, type)

        return Result.success()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationHelper.DAY_CHANNEL_ID,
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
                    NotificationHelper.NIGHT_CHANNEL_ID,
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

    private fun showNotification(context: Context, type: String) {

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context,
            if (type == "Day") NotificationHelper.DAY_CHANNEL_ID
            else NotificationHelper.NIGHT_CHANNEL_ID
        )
            .setContentTitle("$type Skincare Routine")
            .setContentText("Time to start your $type skincare routine!")
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(type.hashCode(), notification)

        Log.d("NotificationWorker", "Notification for $type routine displayed")
    }

    private fun scheduleNextNotification(context: Context, type: String) {
        val notificationHelper = NotificationHelper(context)
        val userPreferences = UserPreferences(context)
        if (userPreferences.areNotificationsEnabled()) {
            val currentTime = Calendar.getInstance()
            if (type == "Day") {
//                notificationHelper.scheduleRoutineNotification(
//                    requestCode = NotificationHelper.DAY_NOTIFICATION_REQUEST_CODE,
//                    hour = 6,
//                    minute = 0,
//                    type = "Day"
//                )
                notificationHelper.scheduleRoutineNotification(
                    requestCode = NotificationHelper.DAY_NOTIFICATION_REQUEST_CODE,
                    hour = currentTime.get(Calendar.HOUR_OF_DAY),
                    minute = currentTime.get(Calendar.MINUTE),
                    second = currentTime.get(Calendar.SECOND) + 60,
                    type = "Day"
                )
            } else if (type == "Night") {
//                notificationHelper.scheduleRoutineNotification(
//                    requestCode = NotificationHelper.NIGHT_NOTIFICATION_REQUEST_CODE,
//                    hour = 20,
//                    minute = 0,
//                    type = "Night"
//                )
                notificationHelper.scheduleRoutineNotification(
                    requestCode = NotificationHelper.NIGHT_NOTIFICATION_REQUEST_CODE,
                    hour = currentTime.get(Calendar.HOUR_OF_DAY),
                    minute = currentTime.get(Calendar.MINUTE),
                    second = currentTime.get(Calendar.SECOND) + 60,
                    type = "Night"
                )
            }
        }
    }

    companion object {
        const val WORK_NAME_DAY = "day_routine_notification"
        const val WORK_NAME_NIGHT = "night_routine_notification"
    }
}