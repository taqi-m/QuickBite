package com.quick.bite.model

data class Order(
    val orderID: Int,
    val userID: String,
    val itemName: String,
    val quantity: Int,
    val itemPrice: Double,
    val totalPrice: Double,
    val masterID: Int
)

data class MasterOrder(
    val masterID: Int,
    val userID: String,
    val usercode: String,
    val restaurantID: Int,
    val grandtotal: Double
)


