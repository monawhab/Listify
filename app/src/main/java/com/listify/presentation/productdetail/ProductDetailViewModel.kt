package com.listify.presentation.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listify.domain.model.Product
import com.listify.domain.repository.CartRepository
import com.listify.domain.usecase.GetProductByIdUseCase
import com.listify.domain.usecase.GetProductsByCategoryUseCase
import com.listify.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val uiState: StateFlow<UiState<Product>> = _uiState.asStateFlow()

    private val _relatedProducts = MutableStateFlow<List<Product>>(emptyList())
    val relatedProducts: StateFlow<List<Product>> = _relatedProducts.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    val cartItemCount: StateFlow<List<*>> = cartRepository.cartItems

    init { loadProduct() }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getProductByIdUseCase(productId)
                .onSuccess { product ->
                    _uiState.value = UiState.Success(product)
                    loadRelated(product.category, product.id)
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Error") }
        }
    }

    private fun loadRelated(category: String, excludeId: Int) {
        viewModelScope.launch {
            getProductsByCategoryUseCase(category).onSuccess { products ->
                _relatedProducts.value = products.filter { it.id != excludeId }
            }
        }
    }

    fun incrementQuantity() { if (_quantity.value < 99) _quantity.value++ }
    fun decrementQuantity() { if (_quantity.value > 1) _quantity.value-- }

    fun addToCart() {
        val product = (_uiState.value as? UiState.Success)?.data ?: return
        cartRepository.addToCart(product, _quantity.value)
    }
}
