package com.listify.domain.usecase

import com.listify.data.remote.api.FakeStoreApi
import com.listify.domain.model.Product
import com.listify.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(limit: Int = FakeStoreApi.PAGE_SIZE): Result<List<Product>> =
        repository.getProducts(limit)
}

class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(id: Int): Result<Product> = repository.getProductById(id)
}

class GetProductsByCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(category: String, limit: Int = 20): Result<List<Product>> =
        repository.getProductsByCategory(category, limit)
}

class GetCategoriesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<String>> = repository.getCategories()
}
