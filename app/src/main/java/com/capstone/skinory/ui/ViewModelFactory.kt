package com.capstone.skinory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.ui.login.LoginViewModel
import com.capstone.skinory.ui.login.TokenDataStore
import com.capstone.skinory.ui.notifications.RoutineViewModel
import com.capstone.skinory.ui.register.RegisterViewModel

class ViewModelFactory(
    private val repository: DataRepository,
    private val tokenDataStore: TokenDataStore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository, tokenDataStore!!) as T
            }
            modelClass.isAssignableFrom(RoutineViewModel::class.java) -> {
                RoutineViewModel(repository, tokenDataStore!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}