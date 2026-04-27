package com.quick.bite.data.db

import android.content.ContentValues
import android.content.Context
import com.quick.bite.data.db.QuickBiteContract.UserEntry
import com.quick.bite.data.db.QuickBiteContract.RestaurantEntry
import com.quick.bite.data.db.QuickBiteContract.ItemEntry
import com.quick.bite.data.db.QuickBiteContract.MasterOrderEntry
import com.quick.bite.data.db.QuickBiteContract.OrderEntry
import com.quick.bite.model.*
import androidx.core.database.sqlite.transaction

/**
 * QuickBiteDatabaseManager provides a high-level interface for all database operations.
 * Handles CRUD (Create, Read, Update, Delete) operations for all entities.
 *
 * Schema and operations are aligned with Fake Restaurant API endpoints.
 * This class manages the lifecycle of the database helper and provides
 * convenient methods to interact with the database from the application.
 */
@Suppress("unused")
class QuickBiteDatabaseManager(context: Context) {

    private val dbHelper = QuickBiteDatabaseHelper(context)

    // ========== SYNC OPERATIONS ==========

    /**
     * Clears existing restaurants and inserts the fresh list from the API.
     * Wrapped in a transaction for performance and data integrity.
     */
    fun syncRestaurants(restaurants: List<Restaurant>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            try {
                // Clear existing data to avoid staleness or duplicates
                delete(RestaurantEntry.TABLE_NAME, null, null)

                restaurants.forEach { restaurant ->
                    val values = ContentValues().apply {
                        put(RestaurantEntry.COLUMN_ID, restaurant.restaurantID) // Use API ID
                        put(RestaurantEntry.COLUMN_NAME, restaurant.restaurantName)
                        put(RestaurantEntry.COLUMN_ADDRESS, restaurant.address)
                        put(RestaurantEntry.COLUMN_TYPE, restaurant.type)
                        put(RestaurantEntry.COLUMN_PARKING_LOT, if (restaurant.parkingLot) 1 else 0)
                    }
                    insert(RestaurantEntry.TABLE_NAME, null, values)
                }
            } finally {
            }
        }
    }

    /**
     * Clears existing menu items for a specific restaurant and inserts the fresh list.
     */
    fun syncMenu(restaurantId: Int, items: List<Item>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            try {
                delete(
                    ItemEntry.TABLE_NAME,
                    "${ItemEntry.COLUMN_RESTAURANT_ID} = ?",
                    arrayOf(restaurantId.toString())
                )

                items.forEach { item ->
                    val values = ContentValues().apply {
                        put(ItemEntry.COLUMN_ID, item.itemID) // Use API ID
                        put(ItemEntry.COLUMN_NAME, item.itemName)
                        put(ItemEntry.COLUMN_DESCRIPTION, item.itemDescription)
                        put(ItemEntry.COLUMN_PRICE, item.itemPrice)
                        put(ItemEntry.COLUMN_RESTAURANT_NAME, item.restaurantName)
                        put(ItemEntry.COLUMN_RESTAURANT_ID, item.restaurantID)
                        put(ItemEntry.COLUMN_IMAGE_URL, item.imageUrl)
                    }
                    insert(ItemEntry.TABLE_NAME, null, values)
                }
            } finally {
            }
        }
    }

    /**
     * Syncs the user's order history from the remote API into the local SQLite database.
     */
    fun syncMasterOrders(userEmail: String, orders: List<MasterOrder>) {
        val db = dbHelper.writableDatabase
        db.transaction {
            try {
                // Clear existing orders for this user to prevent duplicates
                delete(
                    MasterOrderEntry.TABLE_NAME,
                    "${MasterOrderEntry.COLUMN_USER_EMAIL} = ?",
                    arrayOf(userEmail)
                )

                orders.forEach { order ->
                    val values = ContentValues().apply {
                        put(MasterOrderEntry.COLUMN_ID, order.masterID)
                        put(MasterOrderEntry.COLUMN_USER_EMAIL, order.userID)
                        put(MasterOrderEntry.COLUMN_USER_CODE, order.usercode)
                        put(MasterOrderEntry.COLUMN_RESTAURANT_ID, order.restaurantID)
                        put(MasterOrderEntry.COLUMN_GRAND_TOTAL, order.grandtotal)
                    }
                    insert(MasterOrderEntry.TABLE_NAME, null, values)
                }
            } finally {
            }
        }
    }

    // ========== USER OPERATIONS ==========

    /**
     * Register a new user in the database
     * @return The number of rows inserted (1 if successful, -1 if error)
     */
    fun registerUser(userEmail: String, password: String, usercode: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserEntry.COLUMN_EMAIL, userEmail)
            put(UserEntry.COLUMN_PASSWORD, password)
            put(UserEntry.COLUMN_USER_CODE, usercode)
        }
        return db.insert(UserEntry.TABLE_NAME, null, values)
    }

    /**
     * Retrieve a user by email
     */
    fun getUserByEmail(userEmail: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            UserEntry.TABLE_NAME,
            null,
            "${UserEntry.COLUMN_EMAIL} = ?",
            arrayOf(userEmail),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    userEmail = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)),
                    password = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_PASSWORD)),
                    usercode = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_USER_CODE))
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieve all users
     */
    fun getAllUsers(): List<User> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(UserEntry.TABLE_NAME, null, null, null, null, null, null)
        val users = mutableListOf<User>()

        cursor.use {
            while (it.moveToNext()) {
                users.add(
                    User(
                        userEmail = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_PASSWORD)),
                        usercode = it.getString(it.getColumnIndexOrThrow(UserEntry.COLUMN_USER_CODE))
                    )
                )
            }
        }
        return users
    }

    /**
     * Update user password
     */
    fun updateUserPassword(userEmail: String, newPassword: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserEntry.COLUMN_PASSWORD, newPassword)
        }
        return db.update(
            UserEntry.TABLE_NAME,
            values,
            "${UserEntry.COLUMN_EMAIL} = ?",
            arrayOf(userEmail)
        )
    }

    /**
     * Delete a user by email
     */
    fun deleteUser(userEmail: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            UserEntry.TABLE_NAME,
            "${UserEntry.COLUMN_EMAIL} = ?",
            arrayOf(userEmail)
        )
    }

    // ========== RESTAURANT OPERATIONS ==========

    /**
     * Insert a new restaurant
     */
    fun insertRestaurant(restaurant: Restaurant): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(RestaurantEntry.COLUMN_NAME, restaurant.restaurantName)
            put(RestaurantEntry.COLUMN_ADDRESS, restaurant.address)
            put(RestaurantEntry.COLUMN_TYPE, restaurant.type)
            put(RestaurantEntry.COLUMN_PARKING_LOT, if (restaurant.parkingLot) 1 else 0)
        }
        return db.insert(RestaurantEntry.TABLE_NAME, null, values)
    }

    /**
     * Retrieve a restaurant by ID
     */
    fun getRestaurantById(restaurantId: Int): Restaurant? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RestaurantEntry.TABLE_NAME,
            null,
            "${RestaurantEntry.COLUMN_ID} = ?",
            arrayOf(restaurantId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Restaurant(
                    restaurantID = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ID)),
                    restaurantName = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)),
                    address = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ADDRESS)),
                    type = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_TYPE)),
                    parkingLot = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_PARKING_LOT)) == 1
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieve all restaurants
     */
    fun getAllRestaurants(): List<Restaurant> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(RestaurantEntry.TABLE_NAME, null, null, null, null, null, null)
        val restaurants = mutableListOf<Restaurant>()

        cursor.use {
            while (it.moveToNext()) {
                restaurants.add(
                    Restaurant(
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ID)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)),
                        address = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ADDRESS)),
                        type = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_TYPE)),
                        parkingLot = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_PARKING_LOT)) == 1
                    )
                )
            }
        }
        return restaurants
    }

    /**
     * Retrieve restaurants by type
     */
    fun getRestaurantsByType(type: String): List<Restaurant> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RestaurantEntry.TABLE_NAME,
            null,
            "${RestaurantEntry.COLUMN_TYPE} = ?",
            arrayOf(type),
            null,
            null,
            null
        )
        val restaurants = mutableListOf<Restaurant>()

        cursor.use {
            while (it.moveToNext()) {
                restaurants.add(
                    Restaurant(
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ID)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)),
                        address = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ADDRESS)),
                        type = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_TYPE)),
                        parkingLot = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_PARKING_LOT)) == 1
                    )
                )
            }
        }
        return restaurants
    }

    /**
     * Retrieve restaurants by address (substring search)
     */
    fun getRestaurantsByAddress(address: String): List<Restaurant> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RestaurantEntry.TABLE_NAME,
            null,
            "${RestaurantEntry.COLUMN_ADDRESS} LIKE ?",
            arrayOf("%$address%"),
            null,
            null,
            null
        )
        val restaurants = mutableListOf<Restaurant>()

        cursor.use {
            while (it.moveToNext()) {
                restaurants.add(
                    Restaurant(
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ID)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)),
                        address = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ADDRESS)),
                        type = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_TYPE)),
                        parkingLot = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_PARKING_LOT)) == 1
                    )
                )
            }
        }
        return restaurants
    }

    /**
     * Retrieve restaurants by name
     */
    fun getRestaurantsByName(name: String): List<Restaurant> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RestaurantEntry.TABLE_NAME,
            null,
            "${RestaurantEntry.COLUMN_NAME} LIKE ?",
            arrayOf("%$name%"),
            null,
            null,
            null
        )
        val restaurants = mutableListOf<Restaurant>()

        cursor.use {
            while (it.moveToNext()) {
                restaurants.add(
                    Restaurant(
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ID)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)),
                        address = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_ADDRESS)),
                        type = it.getString(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_TYPE)),
                        parkingLot = it.getInt(it.getColumnIndexOrThrow(RestaurantEntry.COLUMN_PARKING_LOT)) == 1
                    )
                )
            }
        }
        return restaurants
    }

    /**
     * Update a restaurant
     */
    fun updateRestaurant(restaurant: Restaurant): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(RestaurantEntry.COLUMN_NAME, restaurant.restaurantName)
            put(RestaurantEntry.COLUMN_ADDRESS, restaurant.address)
            put(RestaurantEntry.COLUMN_TYPE, restaurant.type)
            put(RestaurantEntry.COLUMN_PARKING_LOT, if (restaurant.parkingLot) 1 else 0)
        }
        return db.update(
            RestaurantEntry.TABLE_NAME,
            values,
            "${RestaurantEntry.COLUMN_ID} = ?",
            arrayOf(restaurant.restaurantID.toString())
        )
    }

    /**
     * Delete a restaurant
     */
    fun deleteRestaurant(restaurantId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            RestaurantEntry.TABLE_NAME,
            "${RestaurantEntry.COLUMN_ID} = ?",
            arrayOf(restaurantId.toString())
        )
    }

    // ========== ITEM OPERATIONS ==========

    /**
     * Insert a new item
     */
    fun insertItem(item: Item): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ItemEntry.COLUMN_NAME, item.itemName)
            put(ItemEntry.COLUMN_DESCRIPTION, item.itemDescription)
            put(ItemEntry.COLUMN_PRICE, item.itemPrice)
            put(ItemEntry.COLUMN_RESTAURANT_NAME, item.restaurantName)
            put(ItemEntry.COLUMN_RESTAURANT_ID, item.restaurantID)
            put(ItemEntry.COLUMN_IMAGE_URL, item.imageUrl)
        }
        return db.insert(ItemEntry.TABLE_NAME, null, values)
    }

    /**
     * Retrieve an item by ID
     */
    fun getItemById(itemId: Int): Item? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ItemEntry.TABLE_NAME,
            null,
            "${ItemEntry.COLUMN_ID} = ?",
            arrayOf(itemId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Item(
                    itemID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_ID)),
                    itemName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_NAME)),
                    itemDescription = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_DESCRIPTION)),
                    itemPrice = it.getDouble(it.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE)),
                    restaurantName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_NAME)),
                    restaurantID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_ID)),
                    imageUrl = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_IMAGE_URL))
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieve all items by restaurant ID
     */
    fun getItemsByRestaurantId(restaurantId: Int): List<Item> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ItemEntry.TABLE_NAME,
            null,
            "${ItemEntry.COLUMN_RESTAURANT_ID} = ?",
            arrayOf(restaurantId.toString()),
            null,
            null,
            null
        )
        val items = mutableListOf<Item>()

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Item(
                        itemID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_ID)),
                        itemName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_NAME)),
                        itemDescription = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_DESCRIPTION)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_NAME)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_ID)),
                        imageUrl = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_IMAGE_URL))
                    )
                )
            }
        }
        return items
    }

    /**
     * Retrieve all items
     */
    fun getAllItems(): List<Item> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(ItemEntry.TABLE_NAME, null, null, null, null, null, null)
        val items = mutableListOf<Item>()

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Item(
                        itemID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_ID)),
                        itemName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_NAME)),
                        itemDescription = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_DESCRIPTION)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_NAME)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_ID)),
                        imageUrl = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_IMAGE_URL))
                    )
                )
            }
        }
        return items
    }

    /**
     * Search items by name
     */
    fun searchItemsByName(itemName: String): List<Item> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ItemEntry.TABLE_NAME,
            null,
            "${ItemEntry.COLUMN_NAME} LIKE ?",
            arrayOf("%$itemName%"),
            null,
            null,
            null
        )
        val items = mutableListOf<Item>()

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Item(
                        itemID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_ID)),
                        itemName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_NAME)),
                        itemDescription = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_DESCRIPTION)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_NAME)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_ID)),
                        imageUrl = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_IMAGE_URL))
                    )
                )
            }
        }
        return items
    }

    /**
     * Get items sorted by price
     */
    fun getItemsSortedByPrice(ascending: Boolean = true): List<Item> {
        val db = dbHelper.readableDatabase
        val orderBy = if (ascending) "${ItemEntry.COLUMN_PRICE} ASC" else "${ItemEntry.COLUMN_PRICE} DESC"
        val cursor = db.query(
            ItemEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            orderBy
        )
        val items = mutableListOf<Item>()

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Item(
                        itemID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_ID)),
                        itemName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_NAME)),
                        itemDescription = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_DESCRIPTION)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(ItemEntry.COLUMN_PRICE)),
                        restaurantName = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_NAME)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(ItemEntry.COLUMN_RESTAURANT_ID)),
                        imageUrl = it.getString(it.getColumnIndexOrThrow(ItemEntry.COLUMN_IMAGE_URL))
                    )
                )
            }
        }
        return items
    }

    /**
     * Update an item
     */
    fun updateItem(item: Item): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ItemEntry.COLUMN_NAME, item.itemName)
            put(ItemEntry.COLUMN_DESCRIPTION, item.itemDescription)
            put(ItemEntry.COLUMN_PRICE, item.itemPrice)
            put(ItemEntry.COLUMN_RESTAURANT_NAME, item.restaurantName)
            put(ItemEntry.COLUMN_RESTAURANT_ID, item.restaurantID)
            put(ItemEntry.COLUMN_IMAGE_URL, item.imageUrl)
        }
        return db.update(
            ItemEntry.TABLE_NAME,
            values,
            "${ItemEntry.COLUMN_ID} = ?",
            arrayOf(item.itemID.toString())
        )
    }

    /**
     * Delete an item
     */
    fun deleteItem(itemId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ItemEntry.TABLE_NAME,
            "${ItemEntry.COLUMN_ID} = ?",
            arrayOf(itemId.toString())
        )
    }

    // ========== MASTER ORDER OPERATIONS ==========

    /**
     * Insert a new master order
     */
    fun insertMasterOrder(
        userEmail: String,
        usercode: String,
        restaurantId: Int,
        grandTotal: Double
    ): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(MasterOrderEntry.COLUMN_USER_EMAIL, userEmail)
            put(MasterOrderEntry.COLUMN_USER_CODE, usercode)
            put(MasterOrderEntry.COLUMN_RESTAURANT_ID, restaurantId)
            put(MasterOrderEntry.COLUMN_GRAND_TOTAL, grandTotal)
        }
        return db.insert(MasterOrderEntry.TABLE_NAME, null, values)
    }

    /**
     * Retrieve a master order by ID
     */
    fun getMasterOrderById(masterId: Int): MasterOrder? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            MasterOrderEntry.TABLE_NAME,
            null,
            "${MasterOrderEntry.COLUMN_ID} = ?",
            arrayOf(masterId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                MasterOrder(
                    masterID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_ID)),
                    userID = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_EMAIL)),
                    usercode = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_CODE)),
                    restaurantID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_RESTAURANT_ID)),
                    grandtotal = it.getDouble(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_GRAND_TOTAL))
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieve all master orders for a user
     */
    fun getMasterOrdersByUser(userEmail: String): List<MasterOrder> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            MasterOrderEntry.TABLE_NAME,
            null,
            "${MasterOrderEntry.COLUMN_USER_EMAIL} = ?",
            arrayOf(userEmail),
            null,
            null,
            null
        )
        val masterOrders = mutableListOf<MasterOrder>()

        cursor.use {
            while (it.moveToNext()) {
                masterOrders.add(
                    MasterOrder(
                        masterID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_ID)),
                        userID = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_EMAIL)),
                        usercode = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_CODE)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_RESTAURANT_ID)),
                        grandtotal = it.getDouble(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_GRAND_TOTAL))
                    )
                )
            }
        }
        return masterOrders
    }

    /**
     * Retrieve all master orders
     */
    fun getAllMasterOrders(): List<MasterOrder> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(MasterOrderEntry.TABLE_NAME, null, null, null, null, null, null)
        val masterOrders = mutableListOf<MasterOrder>()

        cursor.use {
            while (it.moveToNext()) {
                masterOrders.add(
                    MasterOrder(
                        masterID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_ID)),
                        userID = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_EMAIL)),
                        usercode = it.getString(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_USER_CODE)),
                        restaurantID = it.getInt(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_RESTAURANT_ID)),
                        grandtotal = it.getDouble(it.getColumnIndexOrThrow(MasterOrderEntry.COLUMN_GRAND_TOTAL))
                    )
                )
            }
        }
        return masterOrders
    }

    /**
     * Delete a master order
     */
    fun deleteMasterOrder(masterId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            MasterOrderEntry.TABLE_NAME,
            "${MasterOrderEntry.COLUMN_ID} = ?",
            arrayOf(masterId.toString())
        )
    }

    // ========== ORDER OPERATIONS ==========

    /**
     * Insert a new individual order
     */
    fun insertOrder(
        userEmail: String,
        itemName: String,
        quantity: Int,
        itemPrice: Double,
        totalPrice: Double,
        masterId: Int
    ): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(OrderEntry.COLUMN_USER_EMAIL, userEmail)
            put(OrderEntry.COLUMN_ITEM_NAME, itemName)
            put(OrderEntry.COLUMN_QUANTITY, quantity)
            put(OrderEntry.COLUMN_ITEM_PRICE, itemPrice)
            put(OrderEntry.COLUMN_TOTAL_PRICE, totalPrice)
            put(OrderEntry.COLUMN_MASTER_ID, masterId)
        }
        return db.insert(OrderEntry.TABLE_NAME, null, values)
    }

    /**
     * Retrieve an individual order by ID
     */
    fun getOrderById(orderId: Int): Order? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            OrderEntry.TABLE_NAME,
            null,
            "${OrderEntry.COLUMN_ID} = ?",
            arrayOf(orderId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Order(
                    orderID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ID)),
                    userID = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_USER_EMAIL)),
                    itemName = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_NAME)),
                    quantity = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_QUANTITY)),
                    itemPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_PRICE)),
                    totalPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_TOTAL_PRICE)),
                    masterID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_MASTER_ID))
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieve all individual orders associated with a specific master order ID.
     * Supports the 'Read' operation for relational data.
     */
    fun getOrdersByMasterId(masterId: Int): List<Order> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            OrderEntry.TABLE_NAME,
            null,
            "${OrderEntry.COLUMN_MASTER_ID} = ?",
            arrayOf(masterId.toString()),
            null, null, null
        )
        val orders = mutableListOf<Order>()
        cursor.use {
            while (it.moveToNext()) {
                orders.add(
                    Order(
                        orderID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ID)),
                        userID = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_USER_EMAIL)),
                        itemName = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_NAME)),
                        quantity = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_QUANTITY)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_PRICE)),
                        totalPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_TOTAL_PRICE)),
                        masterID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_MASTER_ID))
                    )
                )
            }
        }
        return orders
    }

    /**
     * Retrieve all orders for a user
     */
    fun getOrdersByUser(userEmail: String): List<Order> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            OrderEntry.TABLE_NAME,
            null,
            "${OrderEntry.COLUMN_USER_EMAIL} = ?",
            arrayOf(userEmail),
            null,
            null,
            null
        )
        val orders = mutableListOf<Order>()

        cursor.use {
            while (it.moveToNext()) {
                orders.add(
                    Order(
                        orderID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ID)),
                        userID = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_USER_EMAIL)),
                        itemName = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_NAME)),
                        quantity = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_QUANTITY)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_PRICE)),
                        totalPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_TOTAL_PRICE)),
                        masterID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_MASTER_ID))
                    )
                )
            }
        }
        return orders
    }

    /**
     * Retrieve all orders
     */
    fun getAllOrders(): List<Order> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(OrderEntry.TABLE_NAME, null, null, null, null, null, null)
        val orders = mutableListOf<Order>()

        cursor.use {
            while (it.moveToNext()) {
                orders.add(
                    Order(
                        orderID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ID)),
                        userID = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_USER_EMAIL)),
                        itemName = it.getString(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_NAME)),
                        quantity = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_QUANTITY)),
                        itemPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_ITEM_PRICE)),
                        totalPrice = it.getDouble(it.getColumnIndexOrThrow(OrderEntry.COLUMN_TOTAL_PRICE)),
                        masterID = it.getInt(it.getColumnIndexOrThrow(OrderEntry.COLUMN_MASTER_ID))
                    )
                )
            }
        }
        return orders
    }

    /**
     * Delete an individual order
     */
    fun deleteOrder(orderId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            OrderEntry.TABLE_NAME,
            "${OrderEntry.COLUMN_ID} = ?",
            arrayOf(orderId.toString())
        )
    }

    /**
     * Delete all orders in a master order
     */
    fun deleteOrdersByMasterId(masterId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            OrderEntry.TABLE_NAME,
            "${OrderEntry.COLUMN_MASTER_ID} = ?",
            arrayOf(masterId.toString())
        )
    }

    // ========== LOCAL CART OPERATIONS ==========

    fun addOrUpdateCartItem(itemId: Int, restaurantId: Int) {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            QuickBiteContract.CartEntry.TABLE_NAME,
            arrayOf(QuickBiteContract.CartEntry.COLUMN_QUANTITY),
            "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = ?",
            arrayOf(itemId.toString()), null, null, null
        )

        var currentQty = 0
        if (cursor.moveToFirst()) currentQty = cursor.getInt(0)
        cursor.close()

        val values = ContentValues().apply {
            put(QuickBiteContract.CartEntry.COLUMN_ITEM_ID, itemId)
            put(QuickBiteContract.CartEntry.COLUMN_RESTAURANT_ID, restaurantId)
            put(QuickBiteContract.CartEntry.COLUMN_QUANTITY, currentQty + 1)
        }

        if (currentQty == 0) db.insert(QuickBiteContract.CartEntry.TABLE_NAME, null, values)
        else db.update(QuickBiteContract.CartEntry.TABLE_NAME, values, "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = ?", arrayOf(itemId.toString()))
    }

    fun decreaseCartItem(itemId: Int) {
        val db = dbHelper.writableDatabase
        val cursor = db.query(QuickBiteContract.CartEntry.TABLE_NAME, arrayOf(QuickBiteContract.CartEntry.COLUMN_QUANTITY), "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = ?", arrayOf(itemId.toString()), null, null, null)

        if (cursor.moveToFirst()) {
            val qty = cursor.getInt(0)
            if (qty > 1) {
                val values = ContentValues().apply { put(QuickBiteContract.CartEntry.COLUMN_QUANTITY, qty - 1) }
                db.update(QuickBiteContract.CartEntry.TABLE_NAME, values, "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = ?", arrayOf(itemId.toString()))
            } else {
                removeCartItem(itemId)
            }
        }
        cursor.close()
    }

    fun removeCartItem(itemId: Int) {
        dbHelper.writableDatabase.delete(QuickBiteContract.CartEntry.TABLE_NAME, "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = ?", arrayOf(itemId.toString()))
    }

    fun clearCart() {
        dbHelper.writableDatabase.delete(QuickBiteContract.CartEntry.TABLE_NAME, null, null)
    }

    fun getCartItemCount(): Int {
        val cursor = dbHelper.readableDatabase.rawQuery("SELECT SUM(${QuickBiteContract.CartEntry.COLUMN_QUANTITY}) FROM ${QuickBiteContract.CartEntry.TABLE_NAME}", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getCartLineItems(): List<Pair<Item, Int>> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT c.${QuickBiteContract.CartEntry.COLUMN_QUANTITY}, i.* FROM ${QuickBiteContract.CartEntry.TABLE_NAME} c 
            INNER JOIN ${QuickBiteContract.ItemEntry.TABLE_NAME} i ON c.${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} = i.${QuickBiteContract.ItemEntry.COLUMN_ID}
        """.trimIndent()

        val items = mutableListOf<Pair<Item, Int>>()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val qty = it.getInt(0)
                val item = Item(
                    itemID = it.getInt(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_ID)),
                    itemName = it.getString(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_NAME)),
                    itemDescription = it.getString(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION)),
                    itemPrice = it.getDouble(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_PRICE)),
                    restaurantName = it.getString(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_NAME)),
                    restaurantID = it.getInt(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID)),
                    imageUrl = it.getString(it.getColumnIndexOrThrow(QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL))
                )
                items.add(Pair(item, qty))
            }
        }
        return items
    }

    /**
     * Close the database helper
     */
    fun close() {
        dbHelper.close()
    }
}
