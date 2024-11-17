package com.capstone.skinory.data

import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.retrofit.ApiService

class DataRepository(private val apiService: ApiService) {
    suspend fun registerUser(name: String, email: String, password: String): Result<RegisterResponse>{
        return try {
            val response = apiService.register(name, email, password)
            Result.success(response)
        } catch (e: Exception){
            Result.failure(e)
        }
    }
}