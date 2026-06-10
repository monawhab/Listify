package com.listify.data.repository
import com.listify.domain.model.CartItem
import com.listify.domain.model.Product
import com.listify.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override fun addToCart(product: Product, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + quantity)
        } else {
            current.add(CartItem(product, quantity))
        }
        _cartItems.value = current
    }
    override fun removeFromCart(productId: Int) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }
    override fun clearCart() { _cartItems.value = emptyList() }
    override fun getItemCount(): Int = _cartItems.value.sumOf { it.quantity }
}
