package com.listify.presentation.productdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.listify.R
import com.listify.databinding.FragmentProductDetailBinding
import com.listify.domain.model.Product
import com.listify.presentation.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductDetailViewModel by viewModels()
    private lateinit var relatedAdapter: RelatedProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRelatedProducts()
        setupQuantityControls()
        setupAddToCart()
        observeState()
    }

    private fun setupRelatedProducts() {
        relatedAdapter = RelatedProductAdapter { product ->
            // Navigate to the tapped related product's detail screen
            val action = ProductDetailFragmentDirections
                .actionProductDetailFragmentSelf(product.id)
            findNavController().navigate(action)
        }
        binding.recyclerRelated.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerRelated.adapter = relatedAdapter
    }

    private fun setupQuantityControls() {
        binding.btnIncrement.setOnClickListener { viewModel.incrementQuantity() }
        binding.btnDecrement.setOnClickListener { viewModel.decrementQuantity() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quantity.collect { binding.tvQuantity.text = it.toString() }
            }
        }
    }

    private fun setupAddToCart() {
        binding.btnAddToCart.setOnClickListener {
            viewModel.addToCart()
            Toast.makeText(
                requireContext(),
                "Added ${viewModel.quantity.value} item(s) to cart!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> Unit
                            is UiState.Success -> bindProduct(state.data)
                            is UiState.Error ->
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                launch {
                    viewModel.relatedProducts.collect { related ->
                        relatedAdapter.submitList(related)
                        val hasRelated = related.isNotEmpty()
                        binding.tvRelatedTitle.isVisible = hasRelated
                        binding.recyclerRelated.isVisible = hasRelated
                    }
                }
            }
        }
    }

    private fun bindProduct(product: Product) {
        binding.apply {
            ivProductImage.load(product.imageUrl) {
                crossfade(300)
                transformations(RoundedCornersTransformation(12f))
            }
            tvCategory.text = product.category.uppercase()
            tvProductName.text = product.title
            tvPrice.text = getString(R.string.price_format, product.price)
            tvDescription.text = product.description
            ratingBar.rating = product.rating.rate.toFloat()
            tvRatingCount.text = "(${product.rating.count} reviews)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
