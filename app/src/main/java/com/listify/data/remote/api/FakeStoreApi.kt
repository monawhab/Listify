package com.listify.data.remote.api

import com.listify.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApi {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductDto

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<ProductDto>

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    companion object {
        const val BASE_URL = "https://fakestoreapi.com/"
    }
}
