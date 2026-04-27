package com.quick.bite.data.api

import com.quick.bite.model.*
import com.quick.bite.model.others.OrderRequest
import retrofit2.http.*
import retrofit2.Response

@Suppress("unused")
interface QuickBiteApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body user: User): User

    @POST("api/auth/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<User>

    @GET("api/users/{userID}")
    suspend fun getUserById(@Path("userID") userID: Long): User

    // Restaurants
    @GET("api/restaurants")
    suspend fun getRestaurants(): List<Restaurant>

    @GET("api/restaurants/{id}")
    suspend fun getRestaurantById(@Path("id") id: Int): Restaurant

    // Items
    @GET("api/items")
    suspend fun getItems(): List<Item>

    @GET("api/items/{id}")
    suspend fun getItemById(@Path("id") id: Int): Item

    @GET("api/items/restaurant/{restaurantID}")
    suspend fun getItemsByRestaurant(@Path("restaurantID") restaurantID: Int): List<Item>

    // Cart
    @GET("api/cart/{userID}")
    suspend fun getCart(@Path("userID") userID: String): Cart

    @POST("api/cart/{userID}")
    suspend fun updateCart(
        @Path("userID") userID: String,
        @Body cartUpdate: Map<String, Int>
    ): Cart  // Server now returns Cart with totalAmount

    @DELETE("api/cart/{userID}/{itemID}")
    suspend fun deleteCartItem(
        @Path("userID") userID: String,
        @Path("itemID") itemID: Int
    ): Cart  // Server returns updated Cart with totalAmount

    @DELETE("api/cart/{userID}")
    suspend fun clearCart(@Path("userID") userID: String): Cart  // Server returns Cart with totalAmount

    // Orders
    @POST("api/orders")
    suspend fun createOrder(@Body orderRequest: OrderRequest): Order  // Server returns Order with totalAmount

    @GET("api/orders")
    suspend fun getAllOrders(): List<Order>  // Each order has totalAmount

    @GET("api/orders/user/{userID}")
    suspend fun getUserOrders(@Path("userID") userID: Long): List<Order>  // Each order has totalAmount

    @DELETE("api/orders/{orderID}")
    suspend fun cancelOrder(@Path("orderID") orderID: Long): Response<GenericResponse>
}