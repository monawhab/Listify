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
import com.listify.R
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
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                if (productAdapter.getItemViewType(position) == 1) 2 else 1
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = skeletonAdapter
        binding.recyclerView.isVisible = true
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText.orEmpty()); return true
            }
        })
    }

    private fun setupRetry() {
        binding.layoutError.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRetry)
            ?.setOnClickListener { viewModel.loadProducts(reset = true) }
    }

    private fun setupPagination() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as GridLayoutManager
                if (lm.findLastVisibleItemPosition() >= lm.itemCount - 4) viewModel.loadNextPage()
            }
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.recyclerView.isVisible = false
                        binding.layoutEmpty.isVisible = false
                        binding.layoutError.isVisible = false
                        when (state) {
                            is UiState.Loading -> {
                                binding.recyclerView.adapter = skeletonAdapter
                                binding.recyclerView.isVisible = true
                            }
                            is UiState.Success -> {
                                if (state.data.isEmpty()) {
                                    binding.layoutEmpty.isVisible = true
                                } else {
                                    if (binding.recyclerView.adapter != productAdapter)
                                        binding.recyclerView.adapter = productAdapter
                                    productAdapter.submitProducts(
                                        state.data,
                                        viewModel.pagingState.value.isLoadingMore
                                    )
                                    binding.recyclerView.isVisible = true
                                }
                            }
                            is UiState.Error -> {
                                binding.layoutError.isVisible = true
                                binding.layoutError
                                    .findViewById<android.widget.TextView>(R.id.tvErrorMessage)
                                    ?.text = state.message
                            }
                        }
                    }
                }
                launch {
                    viewModel.pagingState.collect { paging ->
                        val s = viewModel.uiState.value
                        if (s is UiState.Success && s.data.isNotEmpty())
                            productAdapter.submitProducts(s.data, paging.isLoadingMore)
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
