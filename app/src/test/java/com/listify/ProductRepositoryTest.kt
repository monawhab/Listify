package com.listify.data.repository

import com.listify.data.remote.api.FakeStoreApi
import com.listify.data.remote.dto.ProductDto
import com.listify.data.remote.dto.RatingDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private val api: FakeStoreApi = mockk()
    private lateinit var repository: ProductRepositoryImpl

    private val fakeDto = ProductDto(1,"Shirt",29.99,"Desc","clothing","url", RatingDto(4.5, 100))

    @Before
    fun setUp() { repository = ProductRepositoryImpl(api) }

    @Test
    fun `getProducts maps DTOs to domain models`() = runTest {
        coEvery { api.getProducts(any(), any()) } returns listOf(fakeDto)
        val result = repository.getProducts()
        assertTrue(result.isSuccess)
        val products = result.getOrThrow()
        assertEquals(1, products.size)
        assertEquals("Shirt", products[0].title)
        assertEquals(29.99, products[0].price, 0.01)
        assertEquals(4.5, products[0].rating.rate, 0.01)
    }

    @Test
    fun `getProducts returns failure on exception`() = runTest {
        coEvery { api.getProducts(any(), any()) } throws RuntimeException("Network error")
        val result = repository.getProducts()
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProductById returns correct product`() = runTest {
        coEvery { api.getProductById(1) } returns fakeDto
        val result = repository.getProductById(1)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().id)
    }

    @Test
    fun `getProductById returns failure on not found`() = runTest {
        coEvery { api.getProductById(999) } throws RuntimeException("404")
        val result = repository.getProductById(999)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getCategories returns list of strings`() = runTest {
        coEvery { api.getCategories() } returns listOf("electronics","clothing")
        val result = repository.getCategories()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `getProductsByCategory filters correctly`() = runTest {
        coEvery { api.getProductsByCategory("clothing", any()) } returns listOf(fakeDto)
        val result = repository.getProductsByCategory("clothing")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().all { it.category == "clothing" })
    }
}
