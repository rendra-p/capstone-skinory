package com.capstone.skinory.data.remote.retrofit

import com.capstone.skinory.data.remote.response.LoginRequest
import com.capstone.skinory.data.remote.response.LoginResponse
import com.capstone.skinory.data.remote.response.RegisterRequest
import com.capstone.skinory.data.remote.response.RegisterResponse
import com.capstone.skinory.data.remote.response.RoutineListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @GET("routine/{userId}/day")
    suspend fun getDayRoutines(
        @Header("Authorization") token: String
    ): RoutineListResponse

    @GET("routine/{userId}/night")
    suspend fun getNightRoutines(
        @Header("Authorization") token: String
    ): RoutineListResponse
}