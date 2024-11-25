package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("loginResult")
	val loginResult: LoginResult? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class LoginResult(

	@field:SerializedName("username")
	val name: String? = null,

	@field:SerializedName("userID")
	val userId: String? = null,

	@field:SerializedName("active_token")
	val token: String? = null
)

data class LoginRequest(
	val email: String,
	val password: String
)
