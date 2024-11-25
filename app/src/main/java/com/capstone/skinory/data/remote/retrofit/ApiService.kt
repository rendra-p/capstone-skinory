package com.capstone.skinory.data.remote.retrofit

import com.capstone.skinory.data.remote.response.LoginRequest
import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.ProductListResponse
import com.capstone.skinory.data.remote.response.RegisterRequest
import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.response.RoutineListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @GET("routine/{user_id}/day")
    suspend fun getDayRoutines(
        @Path("user_id") userId: String,
        @Header("Authorization") token: String
    ): RoutineListResponse

    @GET("routine/{user_id}/night")
    suspend fun getNightRoutines(
        @Path("user_id") userId: String,
        @Header("Authorization") token: String
    ): RoutineListResponse

    @GET("/routine/{user_id}/{category}")
    suspend fun getProductsByCategory(
        @Path("user_id") userId: String,
        @Path("category") category: String,
        @Header("Authorization") token: String
    ): Response<ProductListResponse>

    @POST("/routine/{user_id}/{category}/day")
    suspend fun saveRoutineDay(
        @Path("user_id") userId: String,
        @Path("category") category: String,
        @Header("Authorization") token: String,
        @Body selectedProducts: Map<String, Int>
    ): Response<Void>
}