package com.listify.presentation.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listify.domain.model.Product
import com.listify.domain.usecase.GetCategoriesUseCase
import com.listify.domain.usecase.GetProductsByCategoryUseCase
import com.listify.domain.usecase.GetProductsUseCase
import com.listify.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DEFAULT, PRICE_ASC, PRICE_DESC, TOP_RATED }

data class FilterState(
    val selectedCategory: String? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val searchQuery: String = ""
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getProductsUseCase()
                .onSuccess { products ->
                    allProducts = products
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateCategory(category: String?) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = sortOrder)
        applyFilters()
    }

    fun updateFilters(maxPrice: Double?, minRating: Double?) {
        _filterState.value = _filterState.value.copy(maxPrice = maxPrice, minRating = minRating)
        applyFilters()
    }

    private fun applyFilters() {
        val filter = _filterState.value
        var filtered = allProducts

        // Search
        if (filter.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(filter.searchQuery, ignoreCase = true) ||
                it.category.contains(filter.searchQuery, ignoreCase = true)
            }
        }

        // Category
        filter.selectedCategory?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }

        // Price
        filter.maxPrice?.let { max ->
            filtered = filtered.filter { it.price <= max }
        }

        // Rating
        filter.minRating?.let { min ->
            filtered = filtered.filter { it.rating.rate >= min }
        }

        // Sort
        filtered = when (filter.sortOrder) {
            SortOrder.PRICE_ASC  -> filtered.sortedBy { it.price }
            SortOrder.PRICE_DESC -> filtered.sortedByDescending { it.price }
            SortOrder.TOP_RATED  -> filtered.sortedByDescending { it.rating.rate }
            SortOrder.DEFAULT    -> filtered
        }

        _uiState.value = UiState.Success(filtered)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().onSuccess { _categories.value = it }
        }
    }
}
