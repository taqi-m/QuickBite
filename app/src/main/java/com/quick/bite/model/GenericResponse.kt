package com.quick.bite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenericResponse(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "message") val message: String? = null
)
