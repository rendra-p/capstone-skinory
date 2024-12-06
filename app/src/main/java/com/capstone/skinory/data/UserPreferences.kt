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

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", false)
    }

    fun isAutoStartPermissionRequested(): Boolean {
        return sharedPreferences.getBoolean("auto_start_permission_requested", false)
    }

    fun setAutoStartPermissionRequested(requested: Boolean) {
        sharedPreferences.edit().putBoolean("auto_start_permission_requested", requested).apply()
    }
}