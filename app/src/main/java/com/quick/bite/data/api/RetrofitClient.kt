package com.quick.bite.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for API communication
 * Configured with Moshi for JSON serialization/deserialization
 * Includes HTTP logging for debugging
 */
object RetrofitClient {

    private const val BASE_URL = "https://quick-bite-backend-api.onrender.com"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // Lazy initialization of Moshi
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // Lazy initialization of OkHttpClient with logging and timeouts
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            // Add HTTP logging interceptor
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            addInterceptor(loggingInterceptor)

            // Set timeouts
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        }.build()
    }

    // Lazy initialization of Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Get API service instance
     * @return RestaurantApiService instance for making API calls
     */
    fun getApiService(): QuickBiteApiService {
        return retrofit.create(QuickBiteApiService::class.java)
    }
}

