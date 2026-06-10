package com.listify.data.repository

import com.listify.domain.model.Product
import com.listify.domain.model.Rating
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartRepositoryTest {

    private lateinit var repository: CartRepositoryImpl
    private val product = Product(1, "Shirt", 29.99, "Desc", "clothing", "url", Rating(4.5, 100))

    @Before fun setUp() { repository = CartRepositoryImpl() }

    @Test
    fun `addToCart adds new item`() = runTest {
        repository.addToCart(product, 2)
        val items = repository.cartItems.first()
        assertEquals(1, items.size)
        assertEquals(2, items[0].quantity)
    }

    @Test
    fun `addToCart increments existing item quantity`() = runTest {
        repository.addToCart(product, 1)
        repository.addToCart(product, 3)
        val items = repository.cartItems.first()
        assertEquals(1, items.size)
        assertEquals(4, items[0].quantity)
    }

    @Test
    fun `removeFromCart removes item`() = runTest {
        repository.addToCart(product, 1)
        repository.removeFromCart(product.id)
        val items = repository.cartItems.first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `clearCart empties all items`() = runTest {
        repository.addToCart(product, 1)
        repository.clearCart()
        assertTrue(repository.cartItems.first().isEmpty())
    }

    @Test
    fun `getItemCount returns total quantity`() = runTest {
        repository.addToCart(product, 3)
        repository.addToCart(product.copy(id = 2), 2)
        assertEquals(5, repository.getItemCount())
    }
}
