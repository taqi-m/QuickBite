package com.quick.bite.models

data class Product(
    val id: Int,
    val restaurantId: Int,
    val type: String,
    val name: String,
    val description: String,
    val price: String,
    val rating: String,
    val isVeg: Boolean
)


