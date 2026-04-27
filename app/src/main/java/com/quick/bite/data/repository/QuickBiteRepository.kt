package com.quick.bite.data.repository

import com.quick.bite.api.RestaurantApiService
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.model.Item
import com.quick.bite.model.MasterOrder
import com.quick.bite.model.Order
import com.quick.bite.model.Restaurant
import com.quick.bite.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuickBiteRepository(
    private val apiService: RestaurantApiService,
    private val dbManager: QuickBiteDatabaseManager
) {
    // --- API & Database Fetching ---
    suspend fun getRestaurants(): Result<List<Restaurant>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllRestaurants()
            Result.success(response)
        } catch (e: Exception) {
            val local = dbManager.getAllRestaurants()
            if (local.isNotEmpty()) Result.success(local) else Result.failure(e)
        }
    }

    suspend fun getMenu(restaurantId: Int): Result<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRestaurantMenu(restaurantId)
            Result.success(response)
        } catch (e: Exception) {
            val local = dbManager.getItemsByRestaurantId(restaurantId)
            if (local.isNotEmpty()) Result.success(local) else Result.failure(e)
        }
    }

    // --- Local Entity Management (No API equivalent) ---
    fun getCurrentUser(): User? = dbManager.getAllUsers().firstOrNull()

    fun addToCart(item: Item) = dbManager.addOrUpdateCartItem(item.itemID, item.restaurantID)
    fun decreaseCartItem(itemId: Int) = dbManager.decreaseCartItem(itemId)
    fun removeFromCart(itemId: Int) = dbManager.removeCartItem(itemId)
    fun clearCart() = dbManager.clearCart()
    fun getCartItemCount(): Int = dbManager.getCartItemCount()
    fun getCartLineItems(): List<Pair<Item, Int>> = dbManager.getCartLineItems()

    // --- Orders ---
    suspend fun placeOrder(masterOrder: MasterOrder, items: List<Order>): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1. Format for API
            val orderRequest = mapOf("menuDTO" to items.map { mapOf("itemName" to it.itemName, "quantity" to it.quantity) })

            // Fixed: makeOrder no longer takes an apikey
            val apiResponse = apiService.makeOrder(masterOrder.restaurantID, orderRequest)
            val masterId = apiResponse.fullorder.first().masterID

            // 2. Persist to Local DB
            dbManager.insertMasterOrder(
                userEmail = masterOrder.userID,
                usercode = masterOrder.usercode,
                restaurantId = masterOrder.restaurantID,
                grandTotal = apiResponse.grandTotal
            )
            items.forEach {
                dbManager.insertOrder(it.userID, it.itemName, it.quantity, it.itemPrice, it.totalPrice, masterId)
            }
            Result.success(masterId.toLong())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderHistory(userEmail: String): Result<List<MasterOrder>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getOrders()) // Fixed: no apikey
        } catch (e: Exception) {
            Result.success(dbManager.getMasterOrdersByUser(userEmail))
        }
    }

    suspend fun cancelOrder(masterId: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteMasterOrder(masterId) // Fixed: no apikey
            Result.success(dbManager.deleteMasterOrder(masterId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Performs a fast, local search against the SQLite database.
     */
    suspend fun searchRestaurants(query: String): List<Restaurant> = withContext(Dispatchers.IO) {
        dbManager.getRestaurantsByName(query)
    }
}