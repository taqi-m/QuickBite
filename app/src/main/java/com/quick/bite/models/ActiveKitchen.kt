package com.quick.bite.model

data class ActiveKitchen(
    val restaurantId: Int,
    val icon: String,
    val name: String,
    val category: String,
    val rating: String,
    val deliveryTime: String,
    val deliveryFee: String
)