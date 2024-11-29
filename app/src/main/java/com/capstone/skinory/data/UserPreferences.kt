package com.capstone.skinory.data

import android.content.Context

class UserPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    // Tambahkan fungsi untuk menyimpan status notifikasi
    fun saveNotificationStatus(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", isEnabled).apply()
    }

    // Fungsi untuk mendapatkan status notifikasi, default false jika belum pernah diset
    fun getNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", false)
    }

    // Opsional: Fungsi untuk menyimpan status notifikasi spesifik (day/night)
    fun saveDayNotificationStatus(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("day_notifications_enabled", isEnabled).apply()
    }

    fun saveNightNotificationStatus(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("night_notifications_enabled", isEnabled).apply()
    }

    fun getDayNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean("day_notifications_enabled", false)
    }

    fun getNightNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean("night_notifications_enabled", false)
    }
}