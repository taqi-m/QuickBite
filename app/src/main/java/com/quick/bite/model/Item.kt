package com.quick.bite.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Item(
    @Json(name = "itemID") val itemID: Int = 0,
    @Json(name = "restaurantID") val restaurantID: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "imageUrl") val imageUrl: String = "",
    @Json(name = "typeLabel") val typeLabel: String = "",
    @Json(name = "price") val price: Int = 0,
    @Json(name = "itemRating") val itemRating: Double = 0.0
)

