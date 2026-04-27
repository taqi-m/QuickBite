package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "orderID") val orderID: Long,
    @Json(name = "userID") val userID: Long,
    @Json(name = "orderItems") val orderItems: Any, // Dynamic type based on server.js implementation
    @Json(name = "orderStatus") val orderStatus: String,
    @Json(name = "createdAt") val createdAt: Long,
    @Json(name = "totalAmount") val totalAmount: Double = 0.0  // Added from server
)