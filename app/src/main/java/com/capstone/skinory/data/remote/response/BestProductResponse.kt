package com.capstone.skinory.data.remote.response

import com.google.gson.annotations.SerializedName

data class BestProductResponse(

	@field:SerializedName("Best_Products")
	val bestProducts: List<BestProductsItem?>? = null
)

data class BestProductsItem(

	@field:SerializedName("id_product")
	val idProduct: Int? = null,

	@field:SerializedName("name_product")
	val nameProduct: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("rating")
	val rating: String? = null,

	@field:SerializedName("skin_type")
	val skinType: String? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("store_url")
	val storeUrl: String? = null
)
