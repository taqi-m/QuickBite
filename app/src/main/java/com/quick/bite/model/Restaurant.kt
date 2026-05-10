package com.quick.bite.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@Keep
@JsonClass(generateAdapter = true)
data class Restaurant(
    @Json(name = "restaurantID") val restaurantID: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "imageUrl") val imageUrl: String = "",
    @Json(name = "category") val category: String = "",
    @Json(name = "rating") val rating: Double = 0.0,
    @Json(name = "deliveryTime") val deliveryTime: Int = 0
)
