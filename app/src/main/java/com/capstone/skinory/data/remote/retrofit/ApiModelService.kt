package com.capstone.skinory.data.remote.retrofit

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiModelService {
    @Multipart
    @POST("predict/{user_id}")
    suspend fun uploadImage(
        @Path("user_id") userId: String,
        @Part image: MultipartBody.Part
    ): Response<Void>
}