package com.capstone.skinory.data

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.data.remote.retrofit.ApiConfig
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.TokenDataStore

object Injection {
    private fun provideTokenDataStore(context: Context): TokenDataStore {
        return TokenDataStore.getInstance(context)
    }

    private fun provideUserRepository(context: Context, tokenDataStore: TokenDataStore): DataRepository {
        val apiService = ApiConfig.getApiService(tokenDataStore)
        val userPreferences = UserPreferences(context)
        return DataRepository(apiService, userPreferences)
    }

    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        val tokenDataStore = provideTokenDataStore(context)
        val repository = provideUserRepository(context, tokenDataStore)
        val userPreferences = UserPreferences(context)
        return ViewModelFactory(repository, tokenDataStore, userPreferences)
    }
}