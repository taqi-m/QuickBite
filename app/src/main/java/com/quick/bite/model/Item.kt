package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Item(
    @Json(name = "itemID") val itemID: Int,
    @Json(name = "restaurantID") val restaurantID: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "typeLabel") val typeLabel: String,
    @Json(name = "price") val price: Int,
    @Json(name = "itemRating") val itemRating: Double
)
