package com.quick.bite.model

data class Restaurant(
    val restaurantID: Int,
    val restaurantName: String,
    val address: String,
    val type: String,
    val parkingLot: Boolean
)

