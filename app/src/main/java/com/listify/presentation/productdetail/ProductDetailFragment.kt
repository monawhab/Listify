package com.listify.presentation.productdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuantityControls()
        observeState()
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

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> { /* show shimmer in future */ }
                        is UiState.Success -> bindProduct(state.data)
                        is UiState.Error   -> { /* show error */ }
                    }
                }
            }
        }
    }

    private fun bindProduct(product: Product) {
        binding.apply {
            ivProductImage.load(product.imageUrl) { crossfade(true) }
            tvProductName.text = product.title
            tvCategory.text = product.category
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
