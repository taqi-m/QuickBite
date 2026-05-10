package com.quick.bite.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "orderID") val orderID: Long = 0,
    @Json(name = "userID") val userID: Long = 0,
    @Json(name = "orderItems") val orderItems: Any? = null, // Dynamic type based on server.js implementation
    @Json(name = "orderStatus") val orderStatus: String = "",
    @Json(name = "createdAt") val createdAt: Long = 0,
    @Json(name = "totalAmount") val totalAmount: Double = 0.0  // Added from server
)
