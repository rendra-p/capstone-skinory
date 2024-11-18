package com.capstone.skinory.data

import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.retrofit.ApiService

class DataRepository(private val apiService: ApiService) {
    suspend fun registerUser(username: String, email: String, password: String): Result<RegisterResponse>{
        return try {
            val response = apiService.register(username, email, password)
            Result.success(response)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun loginUser (email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}