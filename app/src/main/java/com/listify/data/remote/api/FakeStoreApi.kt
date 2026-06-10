package com.listify.data.remote.api

import com.listify.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FakeStoreApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "asc"
    ): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductDto

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int = 20
    ): List<ProductDto>

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    companion object {
        const val BASE_URL = "https://fakestoreapi.com/"
        const val PAGE_SIZE = 10
    }
}
