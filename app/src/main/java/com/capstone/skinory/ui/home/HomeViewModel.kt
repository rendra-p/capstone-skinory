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
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _profileError = MutableLiveData<String?>()
    val profileError: LiveData<String?> = _profileError

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _skinType = MutableLiveData<String>()
    val skinType: LiveData<String> = _skinType

    fun fetchTokenAndProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                tokenDataStore.token.collect { token ->
                    token?.let {
                        fetchProfile(it)
                    }
                }
            } catch (e: Exception) {
                _profileError.value = "Failed to fetch token"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchProfile(token: String) {
        try {
            _isLoading.value = true
            val result = repository.getProfile(token)

            result.onSuccess { response ->
                response.profile?.let { profile ->
                    _username.value = profile.username ?: "User"
                    _skinType.value = profile.skinType ?: "Unknown"
                }
            }.onFailure {
                _profileError.value = "Failed to fetch profile"
            }
        } catch (e: Exception) {
            _profileError.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
}