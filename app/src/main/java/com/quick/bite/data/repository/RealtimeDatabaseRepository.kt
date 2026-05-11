package com.quick.bite.data.repository

import com.google.firebase.database.*
import com.quick.bite.model.Item
import com.quick.bite.model.Order
import com.quick.bite.model.Restaurant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RealtimeDatabaseRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    init {
        try {
            database.setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence must be set before any other usage
        }
    }

    private val restaurantsRef = database.getReference("restaurants")
    private val itemsRef = database.getReference("items")
    private val usersRef = database.getReference("users")

    /**
     * Returns a real-time stream of all restaurants.
     */
    fun getRestaurantsStream(): Flow<List<Restaurant>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val restaurants = snapshot.children.mapNotNull { it.getValue(Restaurant::class.java) }
                trySend(restaurants)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        restaurantsRef.addValueEventListener(listener)
        awaitClose { restaurantsRef.removeEventListener(listener) }
    }

    /**
     * Returns a real-time stream of menu items for a specific restaurant.
     */
    fun getItemsStream(restaurantId: Int): Flow<List<Item>> = callbackFlow {
        val query = itemsRef.orderByChild("restaurantID").equalTo(restaurantId.toDouble())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Item::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    /**
     * Returns a real-time stream of the user's cart.
     * Cart structure: users/$userId/cart/$itemId = quantity
     */
    fun getCartStream(userId: String): Flow<Map<Int, Int>> = callbackFlow {
        val cartRef = usersRef.child(userId).child("cart")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cart = mutableMapOf<Int, Int>()
                snapshot.children.forEach { child ->
                    val itemId = child.key?.toIntOrNull()
                    val quantity = child.getValue(Int::class.java)
                    if (itemId != null && quantity != null) {
                        cart[itemId] = quantity
                    }
                }
                trySend(cart)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        cartRef.addValueEventListener(listener)
        awaitClose { cartRef.removeEventListener(listener) }
    }

    /**
     * Updates an item in the cart using a transaction to avoid data races.
     */
    suspend fun updateCartItem(userId: String, itemId: Int, delta: Int) {
        val itemRef = usersRef.child(userId).child("cart").child(itemId.toString())
        itemRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentQuantity = currentData.getValue(Int::class.java) ?: 0
                val newQuantity = currentQuantity + delta
                if (newQuantity <= 0) {
                    currentData.value = null
                } else {
                    currentData.value = newQuantity
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }
    
    suspend fun removeCartItem(userId: String, itemId: Int) {
        usersRef.child(userId).child("cart").child(itemId.toString()).removeValue().await()
    }

    suspend fun clearCart(userId: String) {
        usersRef.child(userId).child("cart").removeValue().await()
    }

    /**
     * Returns a real-time stream of the user's order history.
     */
    fun getOrdersStream(userId: String): Flow<List<Order>> = callbackFlow {
        val ordersRef = usersRef.child(userId).child("orders")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                    .sortedByDescending { it.createdAt }
                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ordersRef.addValueEventListener(listener)
        awaitClose { ordersRef.removeEventListener(listener) }
    }

    /**
     * Places a new order and clears the cart.
     */
    suspend fun placeOrder(userId: String, order: Order) {
        val ordersRef = usersRef.child(userId).child("orders")
        val newOrderRef = ordersRef.push()
        val orderWithId = order.copy(orderID = newOrderRef.key?.hashCode()?.toLong() ?: System.currentTimeMillis())
        newOrderRef.setValue(orderWithId).await()
        clearCart(userId)
    }
    
    suspend fun cancelOrder(userId: String, orderId: String) {
        // In a real app, you'd probably find the order by ID first
        // For simplicity, this assumes orderId is the Firebase key
        usersRef.child(userId).child("orders").child(orderId).child("orderStatus").setValue("CANCELLED").await()
    }
}
