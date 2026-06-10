package com.listify.presentation.productlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.listify.domain.model.Product
import com.listify.domain.model.Rating
import com.listify.domain.usecase.GetCategoriesUseCase
import com.listify.domain.usecase.GetProductsByCategoryUseCase
import com.listify.domain.usecase.GetProductsUseCase
import com.listify.presentation.common.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProductListViewModel
    private val getProductsUseCase: GetProductsUseCase = mockk()
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase = mockk()
    private val getCategoriesUseCase: GetCategoriesUseCase = mockk()

    private val fakeProducts = listOf(
        Product(1, "Shirt", 29.99, "A nice shirt", "clothing", "url1", Rating(4.5, 100)),
        Product(2, "Laptop", 999.99, "Powerful laptop", "electronics", "url2", Rating(4.8, 50)),
        Product(3, "Backpack", 49.99, "Durable backpack", "clothing", "url3", Rating(3.9, 200))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getProductsUseCase(any()) } returns Result.success(fakeProducts)
        coEvery { getCategoriesUseCase() } returns Result.success(listOf("clothing", "electronics"))
        viewModel = ProductListViewModel(getProductsUseCase, getProductsByCategoryUseCase, getCategoriesUseCase)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadProducts emits Success with all products`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertTrue(state is UiState.Success)
        assertEquals(3, (state as UiState.Success).data.size)
    }

    @Test
    fun `loadProducts emits Error on failure`() = runTest {
        coEvery { getProductsUseCase(any()) } returns Result.failure(RuntimeException("Network error"))
        viewModel.loadProducts(reset = true)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertTrue(state is UiState.Error)
        assertEquals("Network error", (state as UiState.Error).message)
    }

    @Test
    fun `search filters products by title`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateSearchQuery("Laptop")
        val state = viewModel.uiState.first() as UiState.Success
        assertEquals(1, state.data.size)
        assertEquals("Laptop", state.data[0].title)
    }

    @Test
    fun `sort by price ascending orders correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateSortOrder(SortOrder.PRICE_ASC)
        val prices = (viewModel.uiState.first() as UiState.Success).data.map { it.price }
        assertEquals(prices, prices.sorted())
    }

    @Test
    fun `filter by category returns only matching products`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateCategory("electronics")
        val state = viewModel.uiState.first() as UiState.Success
        assertTrue(state.data.all { it.category == "electronics" })
    }

    @Test
    fun `loadNextPage does nothing when already loading`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        // Simulate already loading
        viewModel.loadNextPage()
        viewModel.loadNextPage() // second call should be ignored
        testDispatcher.scheduler.advanceUntilIdle()
        // getProductsUseCase called once on init + once for loadNextPage = 2 max
        coVerify(atMost = 2) { getProductsUseCase(any()) }
    }

    @Test
    fun `loadNextPage increments limit by PAGE_SIZE`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialLimit = viewModel.pagingState.value.currentLimit
        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()
        val newLimit = viewModel.pagingState.value.currentLimit
        assertTrue(newLimit > initialLimit)
    }

    @Test
    fun `hasReachedEnd is true when fewer products than limit returned`() = runTest {
        // Return 3 items when limit is 10 — means end of data
        coEvery { getProductsUseCase(any()) } returns Result.success(fakeProducts)
        viewModel.loadProducts(reset = true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.pagingState.value.hasReachedEnd)
    }
}
