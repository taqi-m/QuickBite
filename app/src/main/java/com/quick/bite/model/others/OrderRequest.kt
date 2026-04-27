package com.quick.bite.model.others

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderRequest(
    @Json(name = "userID") val userID: Long,
    @Json(name = "orderItems") val orderItems: Map<String, Int>
)