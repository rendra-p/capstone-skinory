package com.capstone.skinory.data

import android.content.Context
import com.capstone.skinory.data.remote.response.LoginRequest
import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.ProductsItem
import com.capstone.skinory.data.remote.response.RegisterRequest
import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.response.RoutineListResponse
import com.capstone.skinory.data.remote.retrofit.ApiService
import com.capstone.skinory.ui.login.TokenDataStore
import kotlinx.coroutines.flow.first

class DataRepository(private val apiService: ApiService, private val userPreferences: UserPreferences, private val context: Context) {
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
        val userId = userPreferences.getUserId() ?: throw Exception("User  ID not found")
        return try {
            val response = apiService.getDayRoutines(userId, token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNightRoutines(token: String): Result<RoutineListResponse> {
        val userId = userPreferences.getUserId() ?: throw Exception("User  ID not found")
        return try {
            val response = apiService.getNightRoutines(userId, token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(category: String, token: String): List<ProductsItem> {
        val userId = userPreferences.getUserId() ?: throw Exception("User  ID not found")
        val response = apiService.getProductsByCategory(userId, category, token)
        if (response.isSuccessful) {
            return response.body()?.products?.filterNotNull() ?: emptyList()
        } else {
            throw Exception("Error fetching products")
        }
    }

    suspend fun saveRoutineDay(category: String, productId: Int, selectedProducts: Map<String, Int>, token: String
    ): Result<Void?> {
        return try {
            val userId = userPreferences.getUserId() ?: throw Exception("User  ID not found")
            val response = apiService.saveRoutineDay(userId, category, productId, selectedProducts, token)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                throw Exception("Error saving routine: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDayRoutine(token: String): Result<Void?> {
        return try {
            val userId = userPreferences.getUserId() ?: throw Exception("User  ID not found")
            val response = apiService.deleteDayRoutine(userId, token)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error deleting routine: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}