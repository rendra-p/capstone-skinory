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
        val apiService = ApiConfig.getApiService(tokenDataStore) // Pass the tokenDataStore here
        val userPreferences = UserPreferences(context)
        return DataRepository(apiService, userPreferences, context)
    }

    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        val tokenDataStore = provideTokenDataStore(context)
        val repository = provideUserRepository(context, tokenDataStore) // Pass tokenDataStore here
        val userPreferences = UserPreferences(context) // Only create once
        return ViewModelFactory(repository, tokenDataStore, userPreferences)
    }
}