package com.capstone.skinory.data

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.data.remote.retrofit.ApiConfig
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.TokenDataStore

object Injection {
    private fun provideUserRepository(): DataRepository {
        val apiService = ApiConfig.getApiService()
        return DataRepository(apiService)
    }

    private fun provideTokenDataStore(context: Context): TokenDataStore {
        return TokenDataStore.getInstance(context)
    }

    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        val repository = provideUserRepository()
        val tokenDataStore = provideTokenDataStore(context)
        return ViewModelFactory(repository, tokenDataStore)
    }
}