package com.capstone.skinory.ui.notifications.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.capstone.skinory.data.UserPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            val action = intent?.action

            if (action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED) {

                Handler(Looper.getMainLooper()).postDelayed({
                    val userPreferences = UserPreferences(ctx)
                    val notificationHelper = NotificationHelper(ctx)

                    Log.d("BootReceiver", "Received boot completed or package replaced")
                    Log.d("BootReceiver", "Notifications enabled: ${userPreferences.areNotificationsEnabled()}")

                    if (userPreferences.areNotificationsEnabled()) {
                        try {
                            notificationHelper.scheduleNotifications()
                            Log.d("BootReceiver", "Notifications rescheduled successfully")
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Error rescheduling notifications", e)
                        }
                    }
                }, 5000)
            }
        }
    }
}