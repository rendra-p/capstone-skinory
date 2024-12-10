package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class PasswordResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class PasswordRequest(
	val newPassword: String
)
