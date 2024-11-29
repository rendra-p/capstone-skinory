package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class RoutineListResponse(

	@field:SerializedName("routines")
	val routines: List<RoutinesItem?>? = null
)

data class RoutinesItem(

	@field:SerializedName("id_product")
	val idProduct: Int? = null,

	@field:SerializedName("name_product")
	val nameProduct: String? = null,

	@field:SerializedName("applied")
	val applied: String? = null,

	@field:SerializedName("skin_type")
	val skinType: String? = null
)

data class GroupedRoutinesItem(
	val applied: String,
	val products: List<String>
)
