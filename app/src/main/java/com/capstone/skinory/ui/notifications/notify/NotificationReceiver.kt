package com.capstone.skinory.ui.notifications.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.capstone.skinory.R
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.ui.MainActivity
import com.capstone.skinory.ui.notifications.notify.NotificationHelper.Companion.DAY_NOTIFICATION_REQUEST_CODE
import com.capstone.skinory.ui.notifications.notify.NotificationHelper.Companion.NIGHT_NOTIFICATION_REQUEST_CODE
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            val pendingResult = goAsync()

            Log.d("NotificationReceiver", "Broadcast received!")

            try {
                val type = intent?.getStringExtra("type") ?: return

                Log.d("NotificationReceiver", "Broadcast received for $type routine!")

                // Buat notification channel
                createNotificationChannel(ctx)

                // Tampilkan notifikasi
                showNotification(ctx, type)

                // Jadwalkan ulang notifikasi
                rescheduleSpecificNotification(ctx, type)
            } finally {
                // Pastikan untuk menyelesaikan proses
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationHelper.DAY_CHANNEL_ID,
                    "Day Skincare Routine",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    NotificationHelper.NIGHT_CHANNEL_ID,
                    "Night Skincare Routine",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )

            channels.forEach { channel ->
                channel.description = "Skincare Routine Notifications"
                channel.enableLights(true)
                channel.lightColor = Color.BLUE
                channel.enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }

            Log.d("NotificationReceiver", "Notification channels created")
        }
    }

    private fun showNotification(context: Context, type: String) {
        val channelId = if (type == "Day")
            NotificationHelper.DAY_CHANNEL_ID
        else
            NotificationHelper.NIGHT_CHANNEL_ID

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("$type Skincare Routine")
            .setContentText("Time to start your $type skincare routine!")
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(type.hashCode(), notification)

        Log.d("NotificationReceiver", "Notification for $type routine displayed")
    }

    private fun rescheduleSpecificNotification(context: Context, type: String) {
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
                    minute = currentTime.get(Calendar.MINUTE) + 2, // Tambah 1 menit
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
                    minute = currentTime.get(Calendar.MINUTE) + 2, // Tambah 1 menit
                    type = "Night"
                )
            }
        }
    }
}