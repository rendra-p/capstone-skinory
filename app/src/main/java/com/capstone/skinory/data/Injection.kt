package com.capstone.skinory.data

import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.data.remote.retrofit.ApiConfig
import com.capstone.skinory.ui.ViewModelFactory

object Injection {
    private fun provideUserRepository(): DataRepository {
        val apiService = ApiConfig.getApiService()
        return DataRepository(apiService)
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        val repository = provideUserRepository()
        return ViewModelFactory(repository)
    }
}