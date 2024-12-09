package com.capstone.skinory.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.PasswordRequest
import com.capstone.skinory.data.remote.response.PasswordResponse

class SettingsViewModel(
    private val dataRepository: DataRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    suspend fun updatePassword(token: String, passwordRequest: PasswordRequest): Result<PasswordResponse> {
        return dataRepository.editPassword(token, passwordRequest)
    }

    fun setDarkMode(enabled: Boolean) {
        userPreferences.setDarkMode(enabled)

        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun isDarkModeEnabled(): Boolean {
        return userPreferences.isDarkModeEnabled()
    }
}