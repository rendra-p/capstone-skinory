package com.capstone.skinory.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.ProfileResponse
import com.capstone.skinory.ui.login.TokenDataStore
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: DataRepository, private val tokenDataStore: TokenDataStore) : ViewModel() {
    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _skinType = MutableLiveData<String>()
    val skinType: LiveData<String> = _skinType

    fun fetchTokenAndProfile() {
        viewModelScope.launch {
            tokenDataStore.token.collect { token ->
                token?.let {
                    fetchProfile(it)
                }
            }
        }
    }

    private suspend fun fetchProfile(token: String) {
        try {
            val result = repository.getProfile(token)
            _profileResult.value = result

            result.onSuccess { response ->
                response.profile?.let { profile ->
                    _username.value = profile.username ?: "User"
                    _skinType.value = profile.skinType ?: "Unknown"
                }
            }
        } catch (e: Exception) {
            _profileResult.value = Result.failure(e)
        }
    }
}