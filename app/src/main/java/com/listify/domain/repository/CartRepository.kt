package com.listify.domain.repository
import com.listify.domain.model.CartItem
import com.listify.domain.model.Product
import kotlinx.coroutines.flow.StateFlow
interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    fun addToCart(product: Product, quantity: Int)
    fun removeFromCart(productId: Int)
    fun clearCart()
    fun getItemCount(): Int
}
