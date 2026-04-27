package com.quick.bite.data.repository

import android.util.Log
import com.quick.bite.data.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.model.Cart
import com.quick.bite.model.Item
import com.quick.bite.model.Order
import com.quick.bite.model.Restaurant
import com.quick.bite.model.User
import com.quick.bite.model.others.OrderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * QuickBiteRepository serves as the single source of truth.
 * Implements an offline-first strategy by syncing network data to the local DB.
 * Returns Result wrappers for better error handling in the UI.
 */
@Suppress("unused")
class QuickBiteRepository(private val dbManager: QuickBiteDatabaseManager) {

    private val apiService = RetrofitClient.getApiService()
    private val TAG = "QuickBiteRepository"

    /* =========================
       AUTHENTICATION OPERATIONS
    ========================= */

    /**
     * Registers a new user via the API and caches the user locally.
     */
    suspend fun register(username: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = User(
                    userID = 0,
                    username = username,
                    password = password
                )
                val response = apiService.register(requestBody)

                // Cache user locally
                dbManager.syncUser(
                    mapOf(
                        "userID" to response.userID,
                        "username" to response.username,
                        "password" to password
                    )
                )
                response
            }
        }

    /**
     * Logs in a user via the API and falls back to local cache if offline.
     */
    suspend fun login(username: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val credentials = mapOf("username" to username, "password" to password)
                    val response = apiService.login(credentials)
                    if (response.isSuccessful) {
                        val user = response.body()!!
                        // Cache user locally
                        dbManager.syncUser(
                            mapOf(
                                "userID" to user.userID,
                                "username" to user.username,
                                "password" to password
                            )
                        )
                        user
                    } else {
                        throw Exception("Login failed: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    // Fallback to local database
                    Log.w(TAG, "Network login failed, trying local cache", e)
                    val localUser = dbManager.loginUser(username, password)
                    if (localUser != null) {
                        User(
                            userID = localUser["userID"] as Long,
                            username = localUser["username"] as String,
                            password = null
                        )
                    } else {
                        throw Exception("Login failed and no cached credentials found")
                    }
                }
            }
        }

    /**
     * Logs out a user by deleting them from the local database.
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val currentUserId = dbManager.getCurrentUserId()
            if (currentUserId != null) {
                val rowsDeleted = dbManager.logoutUser(currentUserId)
                if (rowsDeleted > 0) {
                    Log.i(TAG, "User logged out successfully")
                } else {
                    throw Exception("Failed to delete user from local database")
                }
            } else {
                Log.w(TAG, "No active user found for logout")
            }
            Unit
        }
    }

    /**
     * Gets the current user's ID from the local database.
     * Returns the first available user's ID since only one user exists locally at a time.
     */
    fun getCurrentUserId(): Result<Long?> {
        return runCatching {
            dbManager.getCurrentUserId()
        }
    }

    fun getCurrentUser(): Result<User> {
        return runCatching {
            val user = dbManager.getCurrentUser() ?: throw Exception("No user logged in")
            user
        }
    }

    /* =========================
       RESTAURANTS (READ-THROUGH CACHE)
    ========================= */

    /**
     * Fetches all restaurants from the network, updates local cache, and returns cached data.
     */
    suspend fun getRestaurants(): Result<List<Restaurant>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getRestaurants()
                val networkData = response.map {
                    mapOf(
                        "restaurantID" to it.restaurantID,
                        "name" to it.name,
                        "imageUrl" to it.imageUrl,
                        "category" to it.category,
                        "rating" to it.rating,
                        "deliveryTime" to it.deliveryTime
                    )
                }
                dbManager.syncRestaurants(networkData)
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for restaurants, using cache", e)
            }

            // Return data from local database (works online and offline)
            dbManager.getAllRestaurants().map {
                Restaurant(
                    restaurantID = (it["restaurantID"] as? Number)?.toInt() ?: 0,
                    name = it["name"] as? String ?: "",
                    imageUrl = it["imageUrl"] as? String ?: "",
                    category = it["category"] as? String ?: "",
                    rating = (it["rating"] as? Number)?.toDouble() ?: 0.0,
                    deliveryTime = (it["deliveryTime"] as? Number)?.toInt() ?: 0
                )
            }
        }
    }

    /**
     * Fetches a single restaurant by ID from the network, updates local cache, and returns it.
     */
    suspend fun getRestaurantById(id: Int): Result<Restaurant> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getRestaurantById(id)
                dbManager.syncSingleRestaurant(
                    mapOf(
                        "restaurantID" to response.restaurantID,
                        "name" to response.name,
                        "imageUrl" to response.imageUrl,
                        "category" to response.category,
                        "rating" to response.rating,
                        "deliveryTime" to response.deliveryTime
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for restaurant $id, using cache", e)
            }

            val localRestaurant = dbManager.getRestaurantById(id)
                ?: throw Exception("Restaurant not found with ID: $id")

            Restaurant(
                restaurantID = (localRestaurant["restaurantID"] as? Number)?.toInt() ?: 0,
                name = localRestaurant["name"] as? String ?: "",
                imageUrl = localRestaurant["imageUrl"] as? String ?: "",
                category = localRestaurant["category"] as? String ?: "",
                rating = (localRestaurant["rating"] as? Number)?.toDouble() ?: 0.0,
                deliveryTime = (localRestaurant["deliveryTime"] as? Number)?.toInt() ?: 0
            )
        }
    }

    /* =========================
       ITEMS (READ-THROUGH CACHE)
    ========================= */

    /**
     * Fetches all items from the network, updates local cache, and returns cached data.
     */
    suspend fun getItems(): Result<List<Item>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getItems()
                val networkData = response.map {
                    mapOf(
                        "itemID" to it.itemID,
                        "restaurantID" to it.restaurantID,
                        "name" to it.name,
                        "description" to it.description,
                        "imageUrl" to it.imageUrl,
                        "typeLabel" to it.typeLabel,
                        "price" to it.price.toDouble(),
                        "itemRating" to it.itemRating
                    )
                }
                dbManager.syncItems(networkData)
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for items, using cache", e)
            }

            dbManager.getAllItems().map {
                Item(
                    itemID = (it["itemID"] as? Number)?.toInt() ?: 0,
                    restaurantID = (it["restaurantID"] as? Number)?.toInt() ?: 0,
                    name = it["name"] as? String ?: "",
                    description = it["description"] as? String ?: "",
                    imageUrl = it["imageUrl"] as? String ?: "",
                    typeLabel = it["typeLabel"] as? String ?: "",
                    price = (it["price"] as? Number)?.toInt() ?: 0,
                    itemRating = (it["itemRating"] as? Number)?.toDouble() ?: 0.0
                )
            }
        }
    }

    /**
     * Fetches a single item by ID from the network, updates local cache, and returns it.
     */
    suspend fun getItemById(id: Int): Result<Item> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getItemById(id)
                dbManager.syncSingleItem(
                    mapOf(
                        "itemID" to response.itemID,
                        "restaurantID" to response.restaurantID,
                        "name" to response.name,
                        "description" to response.description,
                        "imageUrl" to response.imageUrl,
                        "typeLabel" to response.typeLabel,
                        "price" to response.price.toDouble(),
                        "itemRating" to response.itemRating
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for item $id, using cache", e)
            }

            val localItem = dbManager.getItemById(id)
                ?: throw Exception("Item not found with ID: $id")

            Item(
                itemID = (localItem["itemID"] as? Number)?.toInt() ?: 0,
                restaurantID = (localItem["restaurantID"] as? Number)?.toInt() ?: 0,
                name = localItem["name"] as? String ?: "",
                description = localItem["description"] as? String ?: "",
                imageUrl = localItem["imageUrl"] as? String ?: "",
                typeLabel = localItem["typeLabel"] as? String ?: "",
                price = (localItem["price"] as? Number)?.toInt() ?: 0,
                itemRating = (localItem["itemRating"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }

    /**
     * Fetches all items for a specific restaurant from the network, updates cache, and returns them.
     */
    suspend fun getItemsByRestaurant(restaurantID: Int): Result<List<Item>> =
        withContext(Dispatchers.IO) {
            runCatching {
                try {
                    val response = apiService.getItemsByRestaurant(restaurantID)
                    val networkData = response.map {
                        mapOf(
                            "itemID" to it.itemID,
                            "restaurantID" to it.restaurantID,
                            "name" to it.name,
                            "description" to it.description,
                            "imageUrl" to it.imageUrl,
                            "typeLabel" to it.typeLabel,
                            "price" to it.price.toDouble(),
                            "itemRating" to it.itemRating
                        )
                    }
                    // Sync these items to local DB
                    networkData.forEach { item ->
                        dbManager.syncSingleItem(item)
                    }
                } catch (e: Exception) {
                    Log.w(
                        TAG,
                        "Network fetch failed for items of restaurant $restaurantID, using cache",
                        e
                    )
                }

                dbManager.getItemsByRestaurant(restaurantID).map {
                    Item(
                        itemID = (it["itemID"] as? Number)?.toInt() ?: 0,
                        restaurantID = (it["restaurantID"] as? Number)?.toInt() ?: 0,
                        name = it["name"] as? String ?: "",
                        description = it["description"] as? String ?: "",
                        imageUrl = it["imageUrl"] as? String ?: "",
                        typeLabel = it["typeLabel"] as? String ?: "",
                        price = (it["price"] as? Number)?.toInt() ?: 0,
                        itemRating = (it["itemRating"] as? Number)?.toDouble() ?: 0.0
                    )
                }
            }
        }

    /* =========================
   CART OPERATIONS (LOCAL + REMOTE SYNC)
========================= */

    /**
     * Gets the cart for a user - tries network first, falls back to local.
     */
    suspend fun getCart(userID: Long): Result<Cart> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                // Convert Long to String for API path (as per API docs: GET /api/cart/:userID)
                val response = apiService.getCart(userID.toString())
                // Sync cart to local DB - pass Long directly
                dbManager.syncCart(userID, response.items)
                response
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for cart of user $userID, using cache", e)
                // Build Cart from local data
                val localCartItems = dbManager.getCart(userID)
                val itemsMap = mutableMapOf<String, Int>()
                localCartItems.forEach { item ->
                    val itemID = (item["itemID"] as? Number)?.toInt()
                    val quantity = (item["quantity"] as? Number)?.toInt()
                    if (itemID != null && quantity != null) {
                        itemsMap[itemID.toString()] = quantity
                    }
                }
                Cart(userID = userID.toString(), items = itemsMap)
            }
        }
    }

    /**
     * Adds an item to the cart or updates its quantity.
     */
    suspend fun addToCart(userID: Long, itemID: Int, quantity: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Pass Long directly to database manager
                dbManager.addToCart(userID, itemID, quantity)
                try {
                    // Convert Long to String for API path
                    apiService.updateCart(
                        userID.toString(),
                        mapOf("itemID" to itemID, "quantity" to quantity)
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync cart add to server, will retry later", e)
                }
                Unit
            }
        }

    /**
     * Gets cart from local database only (no network call).
     */
    fun getLocalCart(userID: Long): Result<List<Map<String, Any?>>> {
        return runCatching { dbManager.getCart(userID) }
    }

    /**
     * Removes a specific item from the user's cart.
     */
    suspend fun removeCartItem(userID: Long, itemID: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                dbManager.removeCartItem(userID, itemID)
                try {
                    apiService.deleteCartItem(userID.toString(), itemID)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync cart item removal to server", e)
                }
                Unit
            }
        }

    /**
     * Clears all items from the user's cart.
     */
    suspend fun clearCart(userID: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            dbManager.clearCart(userID)
            try {
                apiService.clearCart(userID.toString())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync cart clear to server", e)
            }
            Unit
        }
    }

    /* =========================
       ORDER OPERATIONS
    ========================= */

    /**
     * Places a new order via the API and saves it locally.
     */
    suspend fun placeOrder(userID: Long, orderItems: Map<String, Int>): Result<Order> =
        withContext(Dispatchers.IO) {
            runCatching {
                // First, ensure the user exists in local DB
                val existingUser = dbManager.getUserById(userID)
                if (existingUser == null) {
                    // Sync user from API if not found locally
                    try {
                        val userResponse = apiService.getUserById(userID)
                        dbManager.syncUser(
                            mapOf(
                                "userID" to userResponse.userID,
                                "username" to userResponse.username,
                                "password" to null
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("QuickBiteRepo", "Could not fetch user $userID", e)
                        throw Exception("User $userID not found locally or remotely")
                    }
                }

                val orderRequest = OrderRequest(
                    userID = userID,
                    orderItems = orderItems
                )
                val remoteOrder = apiService.createOrder(orderRequest)

                Log.d("QuickBiteRepo", "Order created: ID=${remoteOrder.orderID}, Total=${remoteOrder.totalAmount}")

                // Cache the order locally with totalAmount
                val syncResult = dbManager.syncOrder(
                    mapOf(
                        "orderID" to remoteOrder.orderID,
                        "userID" to remoteOrder.userID,
                        "orderItems" to remoteOrder.orderItems,
                        "orderStatus" to remoteOrder.orderStatus,
                        "createdAt" to remoteOrder.createdAt,
                        "totalAmount" to remoteOrder.totalAmount  // Make sure this is included
                    )
                )

                if (syncResult == -1L) {
                    Log.e("QuickBiteRepo", "Failed to sync order to local DB")
                } else {
                    Log.d("QuickBiteRepo", "Order synced to local DB with ID: $syncResult")
                }

                remoteOrder
            }
        }


    /**
     * Gets all orders from the API and syncs to local cache.
     */
    suspend fun getAllOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getAllOrders()
                // Sync each order to local DB
                response.forEach { order ->
                    dbManager.syncOrder(
                        mapOf(
                            "orderID" to order.orderID,
                            "userID" to order.userID,
                            "orderItems" to order.orderItems.toString(),
                            "orderStatus" to order.orderStatus,
                            "createdAt" to order.createdAt,
                            "totalAmount" to order.totalAmount
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for all orders, using cache", e)
            }

            dbManager.getAllOrders().map {
                Order(
                    orderID = (it["orderID"] as? Number)?.toLong() ?: 0L,
                    userID = (it["userID"] as? Number)?.toLong() ?: 0L,
                    orderItems = it["orderItems"] ?: "",
                    orderStatus = it["orderStatus"] as? String ?: "PENDING",
                    createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L,
                    totalAmount = (it["totalAmount"] as? Number)?.toDouble() ?: 0.0
                )
            }
        }
    }


    /**
     * Gets all orders for a specific user.
     */
    suspend fun getUserOrders(userID: Long): Result<List<Order>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.getUserOrders(userID)
                dbManager.syncUserOrders(
                    userID,
                    response.map { order ->
                        mapOf(
                            "orderID" to order.orderID,
                            "userID" to order.userID,
                            "orderItems" to order.orderItems.toString(),
                            "totalAmount" to order.totalAmount,
                            "orderStatus" to order.orderStatus,
                            "createdAt" to order.createdAt
                        )
                    }
                )
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for orders of user $userID, using cache", e)
            }

            dbManager.getUserOrders(userID).map {
                Order(
                    orderID = (it["orderID"] as? Number)?.toLong() ?: 0L,
                    userID = (it["userID"] as? Number)?.toLong() ?: 0L,
                    orderItems = it["orderItems"] ?: "",
                    totalAmount = (it["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                    orderStatus = it["orderStatus"] as? String ?: "PENDING",
                    createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L
                )
            }
        }
    }

    /**
     * Cancels an order via the API and removes it from local cache.
     */
    suspend fun cancelOrder(orderID: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val response = apiService.cancelOrder(orderID)
                if (response.isSuccessful) {
                    dbManager.cancelOrder(orderID)
                } else {
                    throw Exception("Failed to cancel order: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network cancel failed for order $orderID", e)
                // Still remove from local DB as best effort
                dbManager.cancelOrder(orderID)
            }
            Unit
        }
    }

    /**
     * Gets a single order by ID from local database.
     */
    fun getOrderById(orderID: Long): Result<Order> {
        return runCatching {
            val localOrder = dbManager.getOrderById(orderID)
                ?: throw Exception("Order not found with ID: $orderID")

            Order(
                orderID = (localOrder["orderID"] as? Number)?.toLong() ?: 0L,
                userID = (localOrder["userID"] as? Number)?.toLong() ?: 0L,
                orderItems = localOrder["orderItems"] ?: "",
                orderStatus = localOrder["orderStatus"] as? String ?: "PENDING",
                createdAt = (localOrder["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}