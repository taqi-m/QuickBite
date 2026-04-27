package com.quick.bite.model

data class Cart(
    val items: List<Int>,
    val totalPrice: Double,
    val itemCount: Int
)

