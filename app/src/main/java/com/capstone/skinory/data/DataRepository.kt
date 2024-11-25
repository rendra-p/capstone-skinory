package com.capstone.skinory.data

import com.capstone.skinory.data.remote.response.LoginRequest
import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.RegisterRequest
import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.response.RoutineListResponse
import com.capstone.skinory.data.remote.retrofit.ApiService

class DataRepository(private val apiService: ApiService) {
    suspend fun registerUser(registerRequest: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = apiService.register(registerRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser (loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(loginRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDayRoutines (token: String): Result<RoutineListResponse> {
        return try {
            val response = apiService.getDayRoutines(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNightRoutines(token: String): Result<RoutineListResponse> {
        return try {
            val response = apiService.getNightRoutines(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}