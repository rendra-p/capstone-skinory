package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)