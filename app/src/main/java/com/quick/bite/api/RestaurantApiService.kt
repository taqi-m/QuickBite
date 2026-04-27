package com.quick.bite.api

import com.quick.bite.model.Item
import com.quick.bite.model.MasterOrder
import com.quick.bite.model.OrderResponse
import com.quick.bite.model.Restaurant
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Fake Restaurant API
 * Base URL: https://fakerestaurantapi.runasp.net/api/
 */
interface RestaurantApiService {

    // ========== RESTAURANT ENDPOINTS ==========

    /**
     * Get all restaurants
     * GET /api/Restaurant
     */
    @GET("Restaurant")
    suspend fun getAllRestaurants(): List<Restaurant>

    /**
     * Get restaurant by ID
     * GET /api/Restaurant/{id}
     */
    @GET("Restaurant/{id}")
    suspend fun getRestaurantById(@Path("id") id: Int): List<Restaurant>

    /**
     * Filter restaurants by category/type
     * GET /api/Restaurant?category=type
     */
    @GET("Restaurant")
    suspend fun getRestaurantsByCategory(@Query("category") category: String): List<Restaurant>

    /**
     * Filter restaurants by address
     * GET /api/Restaurant?address=address
     */
    @GET("Restaurant")
    suspend fun getRestaurantsByAddress(@Query("address") address: String): List<Restaurant>

    /**
     * Filter restaurants by name
     * GET /api/Restaurant?name=name
     */
    @GET("Restaurant")
    suspend fun getRestaurantsByName(@Query("name") name: String): List<Restaurant>

    /**
     * Get restaurant menu (items)
     * GET /api/Restaurant/{id}/menu
     */
    @GET("Restaurant/{id}/menu")
    suspend fun getRestaurantMenu(@Path("id") restaurantId: Int): List<Item>

    /**
     * Get restaurant menu sorted by price
     * GET /api/Restaurant/{id}/menu?sortbyprice=asc|desc
     */
    @GET("Restaurant/{id}/menu")
    suspend fun getRestaurantMenuSorted(
        @Path("id") restaurantId: Int,
        @Query("sortbyprice") sortBy: String
    ): List<Item>

    // ========== ITEM ENDPOINTS ==========

    /**
     * Get all items
     * GET /api/Restaurant/items
     */
    @GET("Restaurant/items")
    suspend fun getAllItems(): List<Item>

    /**
     * Search items by name
     * GET /api/Restaurant/items?ItemName=name
     */
    @GET("Restaurant/items")
    suspend fun searchItemsByName(@Query("ItemName") itemName: String): List<Item>

    /**
     * Get all items sorted by price
     * GET /api/Restaurant/items?sortbyprice=asc|desc
     */
    @GET("Restaurant/items")
    suspend fun getAllItemsSorted(@Query("sortbyprice") sortBy: String): List<Item>

    // ========== ORDER ENDPOINTS ==========

    /**
     * Make an order
     * POST /api/Order/{restaurantId}/makeorder
     */
    @POST("Order/{restaurantId}/makeorder")
    suspend fun makeOrder(
        @Path("restaurantId") restaurantId: Int,
        @Body orderRequest: Map<String, List<Map<String, Any>>>
    ): OrderResponse

    /**
     * Get orders
     * GET /api/Order
     */
    @GET("Order")
    suspend fun getOrders(): List<MasterOrder>

    /**
     * Delete master order
     * DELETE /api/Order/master/{masterID}
     */
    @DELETE("Order/master/{masterId}")
    suspend fun deleteMasterOrder(
        @Path("masterId") masterId: Int
    ): Any // We just need it to succeed, we can ignore the response body mapping
}