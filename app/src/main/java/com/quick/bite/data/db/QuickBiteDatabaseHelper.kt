package com.quick.bite.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * QuickBiteDatabaseHelper extends SQLiteOpenHelper to manage database creation
 * and versioning for the QuickBite application.
 *
 * Schema is aligned with Fake Restaurant API endpoints.
 * Handles all database schema operations including:
 * - Creating tables with proper primary keys (autoincrement)
 * - Establishing foreign key relationships between tables
 * - Database upgrades when schema changes
 */
class QuickBiteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        const val DATABASE_NAME = "quick_bite.db"
        const val DATABASE_VERSION = 2

        // =========================
        // USERS TABLE
        // =========================
        private const val SQL_CREATE_USERS_TABLE =
            "CREATE TABLE ${QuickBiteContract.UserEntry.TABLE_NAME} (" +
                    "${QuickBiteContract.UserEntry.COLUMN_USER_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${QuickBiteContract.UserEntry.COLUMN_USERNAME} TEXT NOT NULL," +
                    "${QuickBiteContract.UserEntry.COLUMN_PASSWORD} TEXT NOT NULL" +
                    ")"

        // =========================
        // RESTAURANTS TABLE
        // =========================
        private const val SQL_CREATE_RESTAURANTS_TABLE =
            "CREATE TABLE ${QuickBiteContract.RestaurantEntry.TABLE_NAME} (" +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_NAME} TEXT NOT NULL," +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_IMAGE_URL} TEXT," +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_CATEGORY} TEXT," +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_RATING} REAL," +
                    "${QuickBiteContract.RestaurantEntry.COLUMN_DELIVERY_TIME} INTEGER" +
                    ")"

        // =========================
        // ITEMS TABLE
        // =========================
        private const val SQL_CREATE_ITEMS_TABLE =
            "CREATE TABLE ${QuickBiteContract.ItemEntry.TABLE_NAME} (" +
                    "${QuickBiteContract.ItemEntry.COLUMN_ITEM_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID} INTEGER NOT NULL," +
                    "${QuickBiteContract.ItemEntry.COLUMN_NAME} TEXT NOT NULL," +
                    "${QuickBiteContract.ItemEntry.COLUMN_DESCRIPTION} TEXT," +
                    "${QuickBiteContract.ItemEntry.COLUMN_IMAGE_URL} TEXT," +
                    "${QuickBiteContract.ItemEntry.COLUMN_TYPE_LABEL} TEXT," +
                    "${QuickBiteContract.ItemEntry.COLUMN_PRICE} REAL NOT NULL," +
                    "${QuickBiteContract.ItemEntry.COLUMN_ITEM_RATING} REAL," +
                    "FOREIGN KEY(${QuickBiteContract.ItemEntry.COLUMN_RESTAURANT_ID}) " +
                    "REFERENCES ${QuickBiteContract.RestaurantEntry.TABLE_NAME}(${QuickBiteContract.RestaurantEntry.COLUMN_RESTAURANT_ID}) " +
                    "ON DELETE CASCADE" +
                    ")"

        // =========================
        // CART TABLE
        // =========================
        private const val SQL_CREATE_CART_TABLE =
            "CREATE TABLE ${QuickBiteContract.CartEntry.TABLE_NAME} (" +
                    "${QuickBiteContract.CartEntry.COLUMN_USER_ID} INTEGER NOT NULL," +
                    "${QuickBiteContract.CartEntry.COLUMN_ITEM_ID} INTEGER NOT NULL," +
                    "${QuickBiteContract.CartEntry.COLUMN_QUANTITY} INTEGER NOT NULL," +
                    "PRIMARY KEY(${QuickBiteContract.CartEntry.COLUMN_USER_ID}, ${QuickBiteContract.CartEntry.COLUMN_ITEM_ID})," +
                    "FOREIGN KEY(${QuickBiteContract.CartEntry.COLUMN_ITEM_ID}) " +
                    "REFERENCES ${QuickBiteContract.ItemEntry.TABLE_NAME}(${QuickBiteContract.ItemEntry.COLUMN_ITEM_ID}) " +
                    "ON DELETE CASCADE," +
                    "FOREIGN KEY(${QuickBiteContract.CartEntry.COLUMN_USER_ID}) " +
                    "REFERENCES ${QuickBiteContract.UserEntry.TABLE_NAME}(${QuickBiteContract.UserEntry.COLUMN_USER_ID}) " +
                    "ON DELETE CASCADE" +
                    ")"

        // =========================
        // ORDERS TABLE
        // =========================
        private const val SQL_CREATE_ORDERS_TABLE =
            "CREATE TABLE ${QuickBiteContract.OrderEntry.TABLE_NAME} (" +
                    "${QuickBiteContract.OrderEntry.COLUMN_ORDER_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${QuickBiteContract.OrderEntry.COLUMN_USER_ID} INTEGER NOT NULL," +
                    "${QuickBiteContract.OrderEntry.COLUMN_ORDER_ITEMS} TEXT NOT NULL," +
                    "${QuickBiteContract.OrderEntry.COLUMN_ORDER_STATUS} TEXT NOT NULL," +
                    "${QuickBiteContract.OrderEntry.COLUMN_CREATED_AT} INTEGER NOT NULL," +
                    "totalAmount REAL DEFAULT 0.0," +
                    "FOREIGN KEY(${QuickBiteContract.OrderEntry.COLUMN_USER_ID}) " +
                    "REFERENCES ${QuickBiteContract.UserEntry.TABLE_NAME}(${QuickBiteContract.UserEntry.COLUMN_USER_ID}) " +
                    "ON DELETE CASCADE" +
                    ")"

        // =========================
        // DROP TABLES
        // =========================
        private const val SQL_DROP_USERS =
            "DROP TABLE IF EXISTS ${QuickBiteContract.UserEntry.TABLE_NAME}"

        private const val SQL_DROP_RESTAURANTS =
            "DROP TABLE IF EXISTS ${QuickBiteContract.RestaurantEntry.TABLE_NAME}"

        private const val SQL_DROP_ITEMS =
            "DROP TABLE IF EXISTS ${QuickBiteContract.ItemEntry.TABLE_NAME}"

        private const val SQL_DROP_CART =
            "DROP TABLE IF EXISTS ${QuickBiteContract.CartEntry.TABLE_NAME}"

        private const val SQL_DROP_ORDERS =
            "DROP TABLE IF EXISTS ${QuickBiteContract.OrderEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("PRAGMA foreign_keys = ON")

        db.execSQL(SQL_CREATE_USERS_TABLE)
        db.execSQL(SQL_CREATE_RESTAURANTS_TABLE)
        db.execSQL(SQL_CREATE_ITEMS_TABLE)
        db.execSQL(SQL_CREATE_CART_TABLE)
        db.execSQL(SQL_CREATE_ORDERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL(SQL_DROP_ORDERS)
        db.execSQL(SQL_DROP_CART)
        db.execSQL(SQL_DROP_ITEMS)
        db.execSQL(SQL_DROP_RESTAURANTS)
        db.execSQL(SQL_DROP_USERS)

        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}

