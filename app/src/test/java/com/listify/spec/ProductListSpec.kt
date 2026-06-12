package com.listify.spec

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.listify.domain.model.Product
import com.listify.domain.model.Rating
import com.listify.domain.usecase.GetCategoriesUseCase
import com.listify.domain.usecase.GetProductsByCategoryUseCase
import com.listify.domain.usecase.GetProductsUseCase
import com.listify.presentation.common.UiState
import com.listify.presentation.productlist.ProductListViewModel
import com.listify.presentation.productlist.SortOrder
import io.mockk.coEvery
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

/**
 * SpecKit BDD tests mapping directly to Jira acceptance criteria.
 * Each test reads like the ticket's AC, giving traceability from Jira to test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductListSpec {

    @get:Rule val rule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private val getProducts: GetProductsUseCase = mockk()
    private val getByCategory: GetProductsByCategoryUseCase = mockk()
    private val getCategories: GetCategoriesUseCase = mockk()
    private lateinit var vm: ProductListViewModel

    private val products = listOf(
        Product(1, "Shirt", 29.99, "A shirt", "clothing", "url1", Rating(4.5, 100)),
        Product(2, "Laptop", 999.99, "A laptop", "electronics", "url2", Rating(4.8, 50)),
        Product(3, "Backpack", 49.99, "A backpack", "clothing", "url3", Rating(3.9, 200))
    )

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { getProducts(any()) } returns Result.success(products)
        coEvery { getCategories() } returns Result.success(listOf("clothing", "electronics"))
        vm = ProductListViewModel(getProducts, getByCategory, getCategories)
    }

    @After fun teardown() = Dispatchers.resetMain()

    @Test
    fun `LIS-5 product grid displays products`() = runTest {
        spec("LIS-5: Display product grid with image, name, price, and rating") {
            given("the API returns a list of products") {
                // mocked in setup
            }
            whenever("the product list screen loads") {
                dispatcher.scheduler.advanceUntilIdle()
            }
            then("the grid shows all products") {
                val state = vm.uiState.value
                assertTrue(state is UiState.Success)
                assertEquals(3, (state as UiState.Success).data.size)
            }
            and("each product has a name, price, and rating") {
                val data = (vm.uiState.value as UiState.Success).data
                assertTrue(data.all { it.title.isNotBlank() && it.price > 0 && it.rating.rate >= 0 })
            }
        }
    }

    @Test
    fun `LIS-12 search filters the product list`() = runTest {
        spec("LIS-12: Live search filters products") {
            given("products are loaded") {
                dispatcher.scheduler.advanceUntilIdle()
            }
            whenever("the user searches for 'Laptop'") {
                vm.updateSearchQuery("Laptop")
            }
            then("only matching products are shown") {
                val data = (vm.uiState.value as UiState.Success).data
                assertEquals(1, data.size)
                assertEquals("Laptop", data[0].title)
            }
        }
    }

    @Test
    fun `LIS-14 sort orders products by price`() = runTest {
        spec("LIS-14: Sort products by price low to high") {
            given("products are loaded") {
                dispatcher.scheduler.advanceUntilIdle()
            }
            whenever("the user sorts by price ascending") {
                vm.updateSortOrder(SortOrder.PRICE_ASC)
            }
            then("products appear in ascending price order") {
                val prices = (vm.uiState.value as UiState.Success).data.map { it.price }
                assertEquals(prices.sorted(), prices)
            }
        }
    }

    @Test
    fun `LIS-13 filter by category shows only that category`() = runTest {
        spec("LIS-13: Filter products by category") {
            given("products from multiple categories are loaded") {
                dispatcher.scheduler.advanceUntilIdle()
            }
            whenever("the user filters by 'electronics'") {
                vm.updateCategory("electronics")
            }
            then("only electronics products are shown") {
                val data = (vm.uiState.value as UiState.Success).data
                assertTrue(data.all { it.category == "electronics" })
            }
        }
    }

    @Test
    fun `LIS-8 error state shows when API fails`() = runTest {
        spec("LIS-8: Handle error state on product listing") {
            given("the API will fail") {
                coEvery { getProducts(any()) } returns Result.failure(RuntimeException("Network error"))
            }
            whenever("the user retries loading") {
                vm.loadProducts(reset = true)
                dispatcher.scheduler.advanceUntilIdle()
            }
            then("an error state is shown with the message") {
                val state = vm.uiState.value
                assertTrue(state is UiState.Error)
                assertEquals("Network error", (state as UiState.Error).message)
            }
        }
    }
}
