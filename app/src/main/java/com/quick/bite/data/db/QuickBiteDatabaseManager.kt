package com.quick.bite.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import com.quick.bite.model.User

/**
 * QuickBiteDatabaseManager provides a high-level interface for all database operations.
 * Handles CRUD operations and bulk synchronization for offline-first architecture.
 */
@Suppress("unused")
class QuickBiteDatabaseManager(context: Context) {

    private val dbHelper = QuickBiteDatabaseHelper(context)

    // =========================
    // SYNC OPERATIONS (OFFLINE-FIRST)
    // =========================

    /**
     * Replaces local restaurant data with fresh network data within a transaction.
     */
    fun syncRestaurants(restaurants: List<Map<String, Any?>>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            delete(QuickBiteContract.RestaurantEntry.TABLE_NAME, null, null)
            restaurants.forEach { data ->
                val values = ContentValues().apply {
                    put(QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID, (data["restaurantID"] as? Number)?.toInt())
                    put(QuickBiteContract.RestaurantEntry.COLUMN_NAME, data["name"] as? String)
                    put(QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL, data["imageUrl"] as? String)
                    put(QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY, data["category"] as? String)
                    put(QuickBiteContract.RestaurantEntry.COLUMN_RATING, (data["rating"] as? Number)?.toDouble())
                    put(QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME, (data["deliveryTime"] as? Number)?.toInt())
                }
                insert(QuickBiteContract.RestaurantEntry.TABLE_NAME, null, values)
            }
        }
    }

    /**
     * Replaces local items data with fresh network data.
     */
    fun syncItems(items: List<Map<String, Any?>>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            delete(QuickBiteContract.ItemEntry.TABLE_NAME, null, null)
            items.forEach { data ->
                val values = ContentValues().apply {
                    put(QuickBiteContract.ItemEntry.COLUMN_ITEM_ID, (data["itemID"] as? Number)?.toInt())
                    put(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID, (data["restaurantID"] as? Number)?.toInt())
                    put(QuickBiteContract.ItemEntry.COLUMN_NAME, data["name"] as? String)
                    put(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION, data["description"] as? String)
                    put(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL, data["imageUrl"] as? String)
                    put(QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL, data["typeLabel"] as? String)
                    put(QuickBiteContract.ItemEntry.COLUMN_PRICE, (data["price"] as? Number)?.toDouble())
                    put(QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING, (data["itemRating"] as? Number)?.toDouble())
                }
                insert(QuickBiteContract.ItemEntry.TABLE_NAME, null, values)
            }
        }
    }

    /**
     * Syncs a single restaurant to local DB (upsert).
     */
    fun syncSingleRestaurant(restaurant: Map<String, Any?>) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID, (restaurant["restaurantID"] as? Number)?.toInt())
            put(QuickBiteContract.RestaurantEntry.COLUMN_NAME, restaurant["name"] as? String)
            put(QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL, restaurant["imageUrl"] as? String)
            put(QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY, restaurant["category"] as? String)
            put(QuickBiteContract.RestaurantEntry.COLUMN_RATING, (restaurant["rating"] as? Number)?.toDouble())
            put(QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME, (restaurant["deliveryTime"] as? Number)?.toInt())
        }
        db.insertWithOnConflict(
            QuickBiteContract.RestaurantEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Syncs a single item to local DB (upsert).
     */
    fun syncSingleItem(item: Map<String, Any?>) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.ItemEntry.COLUMN_ITEM_ID, (item["itemID"] as? Number)?.toInt())
            put(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID, (item["restaurantID"] as? Number)?.toInt())
            put(QuickBiteContract.ItemEntry.COLUMN_NAME, item["name"] as? String)
            put(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION, item["description"] as? String)
            put(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL, item["imageUrl"] as? String)
            put(QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL, item["typeLabel"] as? String)
            put(QuickBiteContract.ItemEntry.COLUMN_PRICE, (item["price"] as? Number)?.toDouble())
            put(QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING, (item["itemRating"] as? Number)?.toDouble())
        }
        db.insertWithOnConflict(
            QuickBiteContract.ItemEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // =========================
    // USER OPERATIONS
    // =========================

    /**
     * Registers a new user in the local database.
     * Returns the row ID of the newly inserted user, or -1 on error.
     */
    fun registerUser(username: String, password: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.UserEntry.COLUMN_USERNAME, username)
            put(QuickBiteContract.UserEntry.COLUMN_PASSWORD, password)
        }
        return db.insert(QuickBiteContract.UserEntry.TABLE_NAME, null, values)
    }

    /**
     * Checks if a user exists with the given credentials.
     * Returns the user as a map if found, or null.
     */
    fun loginUser(username: String, password: String): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.UserEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.UserEntry.COLUMN_USERNAME}=? AND ${QuickBiteContract.UserEntry.COLUMN_PASSWORD}=?",
            arrayOf(username, password),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.UserEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USER_ID)),
                QuickBiteContract.UserEntry.COLUMN_USERNAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USERNAME)),
                QuickBiteContract.UserEntry.COLUMN_PASSWORD to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_PASSWORD))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }
    
    

    /**
     * Gets a user by their ID.
     */
    fun getUserById(userID: Long): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.UserEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.UserEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString()),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.UserEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USER_ID)),
                QuickBiteContract.UserEntry.COLUMN_USERNAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USERNAME)),
                QuickBiteContract.UserEntry.COLUMN_PASSWORD to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_PASSWORD))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }

    /**
     * Syncs/upserts a user to the local database.
     */
    fun syncUser(user: Map<String, Any?>): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.UserEntry.COLUMN_USER_ID, (user["userID"] as? Number)?.toLong())
            put(QuickBiteContract.UserEntry.COLUMN_USERNAME, user["username"] as? String)
            put(QuickBiteContract.UserEntry.COLUMN_PASSWORD, user["password"] as? String)
        }
        return db.insertWithOnConflict(
            QuickBiteContract.UserEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Logs out a user by deleting them from the local database.
     * Returns the number of rows deleted.
     */
    fun logoutUser(userID: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            QuickBiteContract.UserEntry.TABLE_NAME,
            "${QuickBiteContract.UserEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString())
        )
    }

    /**
     * Gets the current user's ID from the local database.
     * Since only one user exists locally at a time, returns the first available user's ID.
     * Returns null if no user is currently logged in.
     */
    fun getCurrentUserId(): Long? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.UserEntry.TABLE_NAME,
            arrayOf(QuickBiteContract.UserEntry.COLUMN_USER_ID),
            null, null, null, null,
            "${QuickBiteContract.UserEntry.COLUMN_USER_ID} LIMIT 1"
        )
        val result = if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USER_ID))
        } else {
            null
        }
        cursor.close()
        return result
    }

    fun getCurrentUser(): User? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.UserEntry.TABLE_NAME,
            null, null, null, null, null,
            "${QuickBiteContract.UserEntry.COLUMN_USER_ID} LIMIT 1"
        )
        val user = if (cursor.moveToFirst()) {
            User(
                userID = cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.UserEntry.COLUMN_PASSWORD))
            )
        } else {
            null
        }
        cursor.close()
        return user
    }

    // =========================
    // RETRIEVAL OPERATIONS
    // =========================

    fun getAllRestaurants(): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor = db.query(QuickBiteContract.RestaurantEntry.TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID)),
                QuickBiteContract.RestaurantEntry.COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_NAME)),
                QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL)),
                QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY)),
                QuickBiteContract.RestaurantEntry.COLUMN_RATING to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_RATING)),
                QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME))
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Gets a single restaurant by its ID from the local database.
     */
    fun getRestaurantById(restaurantID: Int): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.RestaurantEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID}=?",
            arrayOf(restaurantID.toString()),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID)),
                QuickBiteContract.RestaurantEntry.COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_NAME)),
                QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL)),
                QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY)),
                QuickBiteContract.RestaurantEntry.COLUMN_RATING to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_RATING)),
                QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }

    fun getAllItems(): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor = db.query(QuickBiteContract.ItemEntry.TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.ItemEntry.COLUMN_ITEM_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_ID)),
                QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID)),
                QuickBiteContract.ItemEntry.COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_NAME)),
                QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION)),
                QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL)),
                QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL)),
                QuickBiteContract.ItemEntry.COLUMN_PRICE to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_PRICE)),
                QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING))
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Gets a single item by its ID from the local database.
     */
    fun getItemById(itemID: Int): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.ItemEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.ItemEntry.COLUMN_ITEM_ID}=?",
            arrayOf(itemID.toString()),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.ItemEntry.COLUMN_ITEM_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_ID)),
                QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID)),
                QuickBiteContract.ItemEntry.COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_NAME)),
                QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION)),
                QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL)),
                QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL)),
                QuickBiteContract.ItemEntry.COLUMN_PRICE to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_PRICE)),
                QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }

    /**
     * Gets all items belonging to a specific restaurant from the local database.
     */
    fun getItemsByRestaurant(restaurantID: Int): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor: Cursor = db.query(
            QuickBiteContract.ItemEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID}=?",
            arrayOf(restaurantID.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.ItemEntry.COLUMN_ITEM_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_ID)),
                QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID)),
                QuickBiteContract.ItemEntry.COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_NAME)),
                QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION)),
                QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL)),
                QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL)),
                QuickBiteContract.ItemEntry.COLUMN_PRICE to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_PRICE)),
                QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING))
            ))
        }
        cursor.close()
        return list
    }

    // =========================
// CART OPERATIONS
// =========================

    fun addToCart(userID: Long, itemID: Int, quantity: Int): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.CartEntry.COLUMN_USER_ID, userID)
            put(QuickBiteContract.CartEntry.COLUMN_ITEM_ID, itemID)
            put(QuickBiteContract.CartEntry.COLUMN_QUANTITY, quantity)
        }
        return db.insertWithOnConflict(
            QuickBiteContract.CartEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Syncs the entire cart for a user from the remote server.
     * Clears existing local cart and replaces with synced data.
     */
    fun syncCart(userID: Long, items: Map<String, Int>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            // Clear existing cart for this user
            delete(
                QuickBiteContract.CartEntry.TABLE_NAME,
                "${QuickBiteContract.CartEntry.COLUMN_USER_ID}=?",
                arrayOf(userID.toString())
            )
            // Insert all items from the map
            items.forEach { (itemIDStr, quantity) ->
                val itemID = itemIDStr.toIntOrNull() ?: return@forEach
                val values = ContentValues().apply {
                    put(QuickBiteContract.CartEntry.COLUMN_USER_ID, userID)
                    put(QuickBiteContract.CartEntry.COLUMN_ITEM_ID, itemID)
                    put(QuickBiteContract.CartEntry.COLUMN_QUANTITY, quantity)
                }
                insert(QuickBiteContract.CartEntry.TABLE_NAME, null, values)
            }
        }
    }

    fun getCart(userID: Long): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor = db.query(
            QuickBiteContract.CartEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.CartEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.CartEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_USER_ID)),
                QuickBiteContract.CartEntry.COLUMN_ITEM_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_ITEM_ID)),
                QuickBiteContract.CartEntry.COLUMN_QUANTITY to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_QUANTITY))
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Gets a specific cart item for a user.
     */
    fun getCartItem(userID: Long, itemID: Int): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.CartEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.CartEntry.COLUMN_USER_ID}=? AND ${QuickBiteContract.CartEntry.COLUMN_ITEM_ID}=?",
            arrayOf(userID.toString(), itemID.toString()),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.CartEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_USER_ID)),
                QuickBiteContract.CartEntry.COLUMN_ITEM_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_ITEM_ID)),
                QuickBiteContract.CartEntry.COLUMN_QUANTITY to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.CartEntry.COLUMN_QUANTITY))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }

    fun removeCartItem(userID: Long, itemID: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            QuickBiteContract.CartEntry.TABLE_NAME,
            "${QuickBiteContract.CartEntry.COLUMN_USER_ID}=? AND ${QuickBiteContract.CartEntry.COLUMN_ITEM_ID}=?",
            arrayOf(userID.toString(), itemID.toString())
        )
    }

    fun clearCart(userID: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            QuickBiteContract.CartEntry.TABLE_NAME,
            "${QuickBiteContract.CartEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString())
        )
    }

    // =========================
// ORDER OPERATIONS
// =========================

    /**
     * Creates a new order in the local database.
     * Returns the row ID of the inserted order.
     */
    fun createOrder(userID: Long, orderItemsJson: String, orderStatus: String = "PENDING"): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.OrderEntry.COLUMN_USER_ID, userID)
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS, orderItemsJson)
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS, orderStatus)
            put(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT, System.currentTimeMillis())
        }
        return db.insert(QuickBiteContract.OrderEntry.TABLE_NAME, null, values)
    }

    /**
     * Syncs an order from the remote server into the local database (upsert).
     * FIXED: Properly converts userID to Long and verifies it exists
     */
    fun syncOrder(order: Map<String, Any?>): Long {
        val db = dbHelper.writableDatabase

        // Extract and validate userID
        val userID = when (val id = order["userID"]) {
            is Number -> id.toLong()
            is String -> id.toLongOrNull()
            else -> null
        }

        if (userID == null) {
            throw IllegalArgumentException("Invalid or missing userID in order: ${order["userID"]}")
        }

        // Verify user exists before inserting order
        val userExists = db.query(
            QuickBiteContract.UserEntry.TABLE_NAME,
            arrayOf(QuickBiteContract.UserEntry.COLUMN_USER_ID),
            "${QuickBiteContract.UserEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString()),
            null, null, null
        ).use { cursor ->
            cursor.moveToFirst()
        }

        if (!userExists) {
            android.util.Log.e("QuickBiteDB", "User $userID not found when syncing order")
            return -1
        }

        // Extract totalAmount - handle both Number and Double
        val totalAmount = when (val amount = order["totalAmount"]) {
            is Number -> amount.toDouble()
            is String -> amount.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        val values = ContentValues().apply {
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_ID, (order["orderID"] as? Number)?.toLong())
            put(QuickBiteContract.OrderEntry.COLUMN_USER_ID, userID)
            // Convert orderItems properly - handle both Map and String
            val orderItemsStr = when (val items = order["orderItems"]) {
                is Map<*, *> -> {
                    // Convert Map to proper JSON string format
                    val jsonMap = items.mapKeys { it.key.toString() }
                        .mapValues { it.value.toString() }
                    jsonMap.toString()
                }
                is String -> items
                else -> order["orderItems"]?.toString() ?: "{}"
            }
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS, orderItemsStr)
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS, order["orderStatus"] as? String ?: "PENDING")
            put(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT, (order["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
            put(QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT, totalAmount)  // Add totalAmount
        }

        android.util.Log.d("QuickBiteDB", "Saving order: ID=${order["orderID"]}, User=$userID, Total=$totalAmount")

        return db.insertWithOnConflict(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }


    /**
     * Syncs all orders for a user by clearing existing and replacing with network data.
     */
    fun syncUserOrders(userID: Long, orders: List<Map<String, Any?>>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            // First verify user exists
            val userExists = db.query(
                QuickBiteContract.UserEntry.TABLE_NAME,
                arrayOf(QuickBiteContract.UserEntry.COLUMN_USER_ID),
                "${QuickBiteContract.UserEntry.COLUMN_USER_ID}=?",
                arrayOf(userID.toString()),
                null, null, null
            ).use { cursor -> cursor.moveToFirst() }

            if (!userExists) {
                android.util.Log.e("QuickBiteDB", "Cannot sync orders: User $userID not found")
                return@transaction
            }

            // Delete existing orders for this user
            delete(
                QuickBiteContract.OrderEntry.TABLE_NAME,
                "${QuickBiteContract.OrderEntry.COLUMN_USER_ID}=?",
                arrayOf(userID.toString())
            )

            // Insert all orders
            orders.forEach { order ->
                val values = ContentValues().apply {
                    put(QuickBiteContract.OrderEntry.COLUMN_ORDER_ID, (order["orderID"] as? Number)?.toLong())
                    put(QuickBiteContract.OrderEntry.COLUMN_USER_ID, userID)
                    val orderItemsStr = when (val items = order["orderItems"]) {
                        is Map<*, *> -> items.toString()
                        is String -> items
                        else -> order["orderItems"]?.toString() ?: "{}"
                    }
                    put(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS, orderItemsStr)
                    put(QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT, (order["totalAmount"] as? Number)?.toDouble() ?: 0.0)
                    put(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS, order["orderStatus"] as? String ?: "PENDING")
                    put(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT, (order["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
                }
                insert(QuickBiteContract.OrderEntry.TABLE_NAME, null, values)
            }
        }
    }

    /**
     * Gets all orders for a specific user from the local database.
     */
    fun getUserOrders(userID: Long): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor: Cursor = db.query(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.OrderEntry.COLUMN_USER_ID}=?",
            arrayOf(userID.toString()),
            null, null,
            "${QuickBiteContract.OrderEntry.COLUMN_CREATED_AT} DESC"
        )
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_USER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS)),
                QuickBiteContract.OrderEntry.COLUMN_CREATED_AT to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT)),
                QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT))
            ))
        }
        cursor.close()
        return list
    }


    /**
     * Gets all orders from the local database.
     */
    fun getAllOrders(): List<Map<String, Any?>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Map<String, Any?>>()
        val cursor: Cursor = db.query(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            null, null, null, null, null,
            "${QuickBiteContract.OrderEntry.COLUMN_CREATED_AT} DESC"
        )
        while (cursor.moveToNext()) {
            list.add(mapOf(
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_USER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_USER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS)),
                QuickBiteContract.OrderEntry.COLUMN_CREATED_AT to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT)),
                QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT to cursor.getDouble(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_TOTAL_AMOUNT))
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Gets a single order by its ID.
     */
    fun getOrderById(orderID: Long): Map<String, Any?>? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            null,
            "${QuickBiteContract.OrderEntry.COLUMN_ORDER_ID}=?",
            arrayOf(orderID.toString()),
            null, null, null
        )
        val result = if (cursor.moveToFirst()) {
            mapOf(
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ID to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_USER_ID to cursor.getInt(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_USER_ID)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS)),
                QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS to cursor.getString(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS)),
                QuickBiteContract.OrderEntry.COLUMN_CREATED_AT to cursor.getLong(cursor.getColumnIndexOrThrow(QuickBiteContract.OrderEntry.COLUMN_CREATED_AT))
            )
        } else {
            null
        }
        cursor.close()
        return result
    }

    /**
     * Cancels an order by removing it from the local database.
     * Returns the number of rows deleted.
     */
    fun cancelOrder(orderID: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            "${QuickBiteContract.OrderEntry.COLUMN_ORDER_ID}=?",
            arrayOf(orderID.toString())
        )
    }

    /**
     * Updates the status of an order in the local database.
     */
    fun updateOrderStatus(orderID: Long, status: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS, status)
        }
        return db.update(
            QuickBiteContract.OrderEntry.TABLE_NAME,
            values,
            "${QuickBiteContract.OrderEntry.COLUMN_ORDER_ID}=?",
            arrayOf(orderID.toString())
        )
    }

    // =========================
    // DATABASE LIFECYCLE
    // =========================

    fun close() {
        dbHelper.close()
    }
}