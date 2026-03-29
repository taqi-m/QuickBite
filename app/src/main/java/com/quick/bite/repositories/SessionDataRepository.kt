package com.quick.bite.repositories

import com.quick.bite.models.CartItem
import com.quick.bite.models.OrderLog
import com.quick.bite.models.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SessionDataRepository {

    private val cartItems = mutableListOf<CartItem>()
    private val orderLogs = mutableListOf<OrderLog>()

    fun addToCart(product: Product, restaurantName: String) {
        val index = cartItems.indexOfFirst { it.productId == product.id }
        val unitPrice = parsePrice(product.price)

        if (index >= 0) {
            val existing = cartItems[index]
            cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(
                CartItem(
                    productId = product.id,
                    restaurantName = restaurantName,
                    productName = product.name,
                    unitPrice = unitPrice,
                    description = product.description,
                    quantity = 1
                )
            )
        }
    }

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun getCartItemCount(): Int = cartItems.sumOf { it.quantity }

    fun getCartSubtotal(): Double = cartItems.sumOf { it.unitPrice * it.quantity }

    fun placeOrder(discountPercent: Double = 0.0): OrderLog? {
        if (cartItems.isEmpty()) return null

        val totalItems = getCartItemCount()
        val subtotal = getCartSubtotal()
        val deliveryFee = if (subtotal > 0.0) 2.0 else 0.0
        val discountAmount = subtotal * discountPercent.coerceIn(0.0, 1.0)
        val total = subtotal + deliveryFee - discountAmount
        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

        val restaurantLabel = cartItems.map { it.restaurantName }.distinct().let { names ->
            if (names.size == 1) names.first() else "Multiple Restaurants"
        }

        val order = OrderLog(
            date = date,
            restaurantName = restaurantLabel,
            details = "$totalItems items • Placed",
            total = formatMoney(total)
        )

        orderLogs.add(0, order)
        cartItems.clear()
        return order
    }

    fun getOrderLogs(): List<OrderLog> = orderLogs.toList()

    private fun parsePrice(price: String): Double {
        return price.replace("$", "").trim().toDoubleOrNull() ?: 0.0
    }

    fun formatMoney(value: Double): String = String.format(Locale.getDefault(), "$%.2f", value)
}

