package com.listify.domain.repository

import com.listify.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(limit: Int = 20): Result<List<Product>>
    suspend fun getProductById(id: Int): Result<Product>
    suspend fun getProductsByCategory(category: String, limit: Int = 20): Result<List<Product>>
    suspend fun getCategories(): Result<List<String>>
}
