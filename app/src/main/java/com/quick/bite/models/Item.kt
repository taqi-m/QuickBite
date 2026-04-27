package com.quick.bite.model

data class Item(
    val itemID: Int,
    val itemName: String,
    val itemDescription: String,
    val itemPrice: Double,
    val restaurantName: String,
    val restaurantID: Int,
    val imageUrl: String
)

