package com.capstone.skinory.ui.notifications.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.capstone.skinory.data.UserPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val notificationHelper = NotificationHelper(it)
                val userPreferences = UserPreferences(it)

                // Jadwalkan ulang notifikasi setelah boot
                if (userPreferences.areNotificationsEnabled()) {
                    notificationHelper.scheduleNotifications()
                }
            }
        }
    }
}