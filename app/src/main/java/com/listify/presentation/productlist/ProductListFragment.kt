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
        observeState()
    }

    private fun setupAdapters() {
        productAdapter = ProductAdapter { product ->
            val action = ProductListFragmentDirections
                .actionProductListFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }
        skeletonAdapter = SkeletonAdapter(count = 6)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        // Start with skeleton
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
        binding.btnRetry.setOnClickListener { viewModel.loadProducts() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.recyclerView.adapter = skeletonAdapter
                            binding.recyclerView.isVisible = true
                            binding.groupError.isVisible = false
                            binding.groupEmpty.isVisible = false
                        }
                        is UiState.Success -> {
                            binding.recyclerView.adapter = productAdapter
                            productAdapter.submitList(state.data)
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
