package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "userID") val userID: Long,
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String? = null
)
