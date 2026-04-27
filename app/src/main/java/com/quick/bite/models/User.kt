package com.quick.bite.model

data class User(
    val userEmail: String,
    val password: String,
    val usercode: String? = null
)

