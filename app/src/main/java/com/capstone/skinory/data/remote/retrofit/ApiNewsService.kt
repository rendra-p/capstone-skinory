package com.capstone.skinory.data.remote.retrofit

import com.capstone.skinory.data.remote.response.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiNewsService {
    @GET("everything?q=skincare&language=en&sortBy=popularity")
    suspend fun getNews(@Query("apiKey") apiKey: String): NewsResponse
}