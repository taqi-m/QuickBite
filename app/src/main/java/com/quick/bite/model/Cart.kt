package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cart(
    @Json(name = "userID") val userID: String,
    @Json(name = "items") val items: Map<String, Int>,
    @Json(name = "totalAmount") val totalAmount: Double = 0.0  // Added from server
)