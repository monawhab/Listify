package com.listify.presentation.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listify.data.remote.api.FakeStoreApi
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

data class PagingState(
    val currentLimit: Int = FakeStoreApi.PAGE_SIZE,
    val isLoadingMore: Boolean = false,
    val hasReachedEnd: Boolean = false
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

    private val _pagingState = MutableStateFlow(PagingState())
    val pagingState: StateFlow<PagingState> = _pagingState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts(reset: Boolean = true) {
        viewModelScope.launch {
            if (reset) {
                _pagingState.value = PagingState()
                _uiState.value = UiState.Loading
            }
            val limit = _pagingState.value.currentLimit
            getProductsUseCase(limit)
                .onSuccess { products ->
                    allProducts = products
                    // FakeStore has 20 total products — detect end of data
                    val hasReachedEnd = products.size < limit || products.size >= 20
                    _pagingState.value = _pagingState.value.copy(
                        isLoadingMore = false,
                        hasReachedEnd = hasReachedEnd
                    )
                    applyFilters()
                }
                .onFailure { error ->
                    _pagingState.value = _pagingState.value.copy(isLoadingMore = false)
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun loadNextPage() {
        val paging = _pagingState.value
        if (paging.isLoadingMore || paging.hasReachedEnd) return
        if (_uiState.value !is UiState.Success) return

        viewModelScope.launch {
            val nextLimit = paging.currentLimit + FakeStoreApi.PAGE_SIZE
            _pagingState.value = paging.copy(isLoadingMore = true, currentLimit = nextLimit)
            loadProducts(reset = false)
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

        if (filter.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(filter.searchQuery, ignoreCase = true) ||
                it.category.contains(filter.searchQuery, ignoreCase = true)
            }
        }
        filter.selectedCategory?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }
        filter.maxPrice?.let { max ->
            filtered = filtered.filter { it.price <= max }
        }
        filter.minRating?.let { min ->
            filtered = filtered.filter { it.rating.rate >= min }
        }
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
