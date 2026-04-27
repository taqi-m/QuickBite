package com.quick.bite.data.db

/**
 * Database Contract - defines all table names and column names
 * Schema aligned with Fake Restaurant API
 */
@Suppress("unused")
object QuickBiteContract {

    const val DATABASE_NAME = "quick_bite_db"

    /* =========================
       USER TABLE
    ========================= */
    object UserEntry {
        const val TABLE_NAME = "users"

        const val COLUMN_USER_ID = "userID"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
    }

    /* =========================
       RESTAURANT TABLE
    ========================= */
    object RestaurantEntry {
        const val TABLE_NAME = "restaurants"

        const val COLUMN_RESTAURANT_ID = "restaurantID"
        const val COLUMN_NAME = "name"
        const val COLUMN_IMAGE_URL = "imageUrl"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_RATING = "rating"
        const val COLUMN_DELIVERY_TIME = "deliveryTime"
    }

    /* =========================
       ITEM TABLE
    ========================= */
    object ItemEntry {
        const val TABLE_NAME = "items"

        const val COLUMN_ITEM_ID = "itemID"
        const val COLUMN_RESTAURANT_ID = "restaurantID"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_IMAGE_URL = "imageUrl"
        const val COLUMN_TYPE_LABEL = "typeLabel"
        const val COLUMN_PRICE = "price"
        const val COLUMN_ITEM_RATING = "itemRating"
    }

    /* =========================
       CART TABLE
       (Map<ItemID, quantity> stored as rows)
    ========================= */
    object CartEntry {
        const val TABLE_NAME = "cart"

        const val COLUMN_USER_ID = "userID"
        const val COLUMN_ITEM_ID = "itemID"
        const val COLUMN_QUANTITY = "quantity"
    }

    /* =========================
       ORDER TABLE
    ========================= */
    object OrderEntry {
        const val TABLE_NAME = "orders"

        const val COLUMN_ORDER_ID = "orderID"
        const val COLUMN_USER_ID = "userID"
        const val COLUMN_ORDER_ITEMS = "orderItems" // JSON string: {itemID: qty}
        const val COLUMN_ORDER_STATUS = "orderStatus"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_TOTAL_AMOUNT = "totalAmount"  // Add this
    }
}
