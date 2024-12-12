package com.capstone.skinory.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.LoginRequest
import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.ProfileResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val repository: DataRepository, private val tokenDataStore: TokenDataStore, private val userPreferences: UserPreferences) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()
    val profileResult: LiveData<Result<ProfileResponse>> = _profileResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val result = repository.loginUser (loginRequest)
                _loginResult.value = result

                result.onSuccess { response ->
                    response.loginResult?.token?.let { token ->
                        tokenDataStore.saveToken(token)

                        response.loginResult.userId?.let { userId ->
                            userPreferences.saveUserId(userId)
                            checkProfile(token)
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e is HttpException && e.code() == 401 -> {
                        "Invalid email or password"
                    }
                    else -> e.message ?: "An unknown error occurred"
                }
                _loginResult.value = Result.failure(Exception(errorMessage))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkProfile(token: String) {
        val profileResult = repository.getProfile(token)
        _profileResult.value = profileResult
    }
}