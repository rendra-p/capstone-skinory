package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProductListResponse(

	@field:SerializedName("products")
	val products: List<ProductsItem> = emptyList()
)

data class ProductsItem(

	@field:SerializedName("id_product")
	val idProduct: Int? = null,

	@field:SerializedName("name_product")
	val nameProduct: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("usage_time")
	val usageTime: String? = null,

	@field:SerializedName("rating")
	val rating: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("skin_type")
	val skinType: String? = null,

	@field:SerializedName("category")
	val category: String? = null
)
