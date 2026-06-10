package com.listify.data.repository

import com.listify.data.remote.api.FakeStoreApi
import com.listify.data.remote.dto.toDomain
import com.listify.domain.model.Product
import com.listify.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: FakeStoreApi
) : ProductRepository {

    override suspend fun getProducts(limit: Int): Result<List<Product>> = runCatching {
        api.getProducts(limit = limit).map { it.toDomain() }
    }

    override suspend fun getProductById(id: Int): Result<Product> = runCatching {
        api.getProductById(id).toDomain()
    }

    override suspend fun getProductsByCategory(category: String, limit: Int): Result<List<Product>> = runCatching {
        api.getProductsByCategory(category, limit).map { it.toDomain() }
    }

    override suspend fun getCategories(): Result<List<String>> = runCatching {
        api.getCategories()
    }
}
