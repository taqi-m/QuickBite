package com.quick.bite.model

import com.squareup.moshi.JsonClass

/**
 * Data class representing the response from the makeorder API endpoint.
 * Maps to the root JSON object containing the list of individual orders and the grand total.
 */
@JsonClass(generateAdapter = true)
data class OrderResponse(
    val fullorder: List<Order>,
    val grandTotal: Double
)