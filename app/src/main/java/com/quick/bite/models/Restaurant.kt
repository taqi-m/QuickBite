package com.quick.bite.models

import java.io.Serializable

data class Restaurant(
    val id: Int,
    val name: String,
    val category: String,
    val rating: String,
    val deliveryTime: String,
    val deliveryFee: String
) : Serializable

