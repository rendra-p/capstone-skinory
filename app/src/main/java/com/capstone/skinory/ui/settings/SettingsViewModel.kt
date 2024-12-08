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

    // Fungsi untuk update password
    suspend fun updatePassword(token: String, passwordRequest: PasswordRequest): Result<PasswordResponse> {
        return dataRepository.editPassword(token, passwordRequest)
    }

    // Fungsi untuk mengatur dark mode
    fun setDarkMode(enabled: Boolean) {
        userPreferences.setDarkMode(enabled)

        // Terapkan tema
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}