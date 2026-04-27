package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Restaurant(
    @Json(name = "restaurantID") val restaurantID: Int,
    @Json(name = "name") val name: String,
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "category") val category: String,
    @Json(name = "rating") val rating: Double,
    @Json(name = "deliveryTime") val deliveryTime: Int
)