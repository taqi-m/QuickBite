package com.quick.bite.models

data class CartItem(
    val productId: Int,
    val restaurantName: String,
    val productName: String,
    val unitPrice: Double,
    val description: String,
    val quantity: Int
)

