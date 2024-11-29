package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(

	@field:SerializedName("Profile")
	val profile: Profile? = null
)

data class Profile(

	@field:SerializedName("skin_type")
	val skinType: String? = null,

	@field:SerializedName("userID")
	val userID: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
