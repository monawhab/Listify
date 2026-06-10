package com.listify.presentation.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.listify.databinding.FragmentProductListBinding
import com.listify.presentation.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductListViewModel by viewModels()

    private lateinit var productAdapter: ProductAdapter
    private lateinit var skeletonAdapter: SkeletonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupSearch()
        setupRetry()
        setupPagination()
        observeState()
    }

    private fun setupAdapters() {
        productAdapter = ProductAdapter { product ->
            val action = ProductListFragmentDirections
                .actionProductListFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }
        skeletonAdapter = SkeletonAdapter(count = 6)

        // GridLayoutManager: footer spans full width, products take 1 column each
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (productAdapter.getItemViewType(position)) {
                    1 -> 2  // LoadingFooter spans full width
                    else -> 1
                }
            }
        }

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = skeletonAdapter
        binding.recyclerView.isVisible = true
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupRetry() {
        binding.btnRetry.setOnClickListener { viewModel.loadProducts(reset = true) }
    }

    private fun setupPagination() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return // only trigger on downward scroll
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                // Trigger load when within 4 items of bottom
                if (lastVisible >= totalItems - 4) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.recyclerView.adapter = skeletonAdapter
                                binding.recyclerView.isVisible = true
                                binding.groupError.isVisible = false
                                binding.groupEmpty.isVisible = false
                            }
                            is UiState.Success -> {
                                val paging = viewModel.pagingState.value
                                if (binding.recyclerView.adapter != productAdapter) {
                                    binding.recyclerView.adapter = productAdapter
                                }
                                productAdapter.submitProducts(
                                    products = state.data,
                                    showLoading = paging.isLoadingMore
                                )
                                binding.recyclerView.isVisible = state.data.isNotEmpty()
                                binding.groupEmpty.isVisible = state.data.isEmpty()
                                binding.groupError.isVisible = false
                            }
                            is UiState.Error -> {
                                binding.recyclerView.isVisible = false
                                binding.groupError.isVisible = true
                                binding.groupEmpty.isVisible = false
                                binding.tvError.text = state.message
                            }
                        }
                    }
                }
                launch {
                    // Re-render list when paging state changes (spinner show/hide)
                    viewModel.pagingState.collect { paging ->
                        val current = viewModel.uiState.value
                        if (current is UiState.Success) {
                            productAdapter.submitProducts(
                                products = current.data,
                                showLoading = paging.isLoadingMore
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
