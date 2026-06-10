package com.listify.presentation.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.listify.databinding.BottomSheetFilterBinding

class FilterBottomSheet(
    private val categories: List<String>,
    private val currentFilter: FilterState,
    private val onApply: (category: String?, maxPrice: Double?, minRating: Double?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String? = currentFilter.selectedCategory
    private var maxPrice: Double? = currentFilter.maxPrice
    private var minRating: Double? = currentFilter.minRating

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategories()
        setupPriceSlider()
        setupRating()
        binding.btnApply.setOnClickListener { onApply(selectedCategory, maxPrice, minRating); dismiss() }
        binding.btnReset.setOnClickListener { onApply(null, null, null); dismiss() }
    }

    private fun setupCategories() {
        binding.chipGroupCategory.removeAllViews()
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.replaceFirstChar { it.uppercase() }
                isCheckable = true
                isChecked = category == selectedCategory
                setOnCheckedChangeListener { _, checked -> if (checked) selectedCategory = category }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupPriceSlider() {
        binding.seekBarPrice.max = 1000
        binding.seekBarPrice.progress = maxPrice?.toInt() ?: 1000
        updatePriceLabel(binding.seekBarPrice.progress)
        binding.seekBarPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                maxPrice = if (p >= 1000) null else p.toDouble()
                updatePriceLabel(p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) = Unit
            override fun onStopTrackingTouch(s: SeekBar?) = Unit
        })
    }

    private fun updatePriceLabel(p: Int) {
        binding.tvPriceLabel.text = if (p >= 1000) "Max Price: Any" else "Max Price: $$p"
    }

    private fun setupRating() {
        binding.ratingBarFilter.rating = minRating?.toFloat() ?: 0f
        binding.ratingBarFilter.setOnRatingBarChangeListener { _, rating, _ ->
            minRating = if (rating == 0f) null else rating.toDouble()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object { const val TAG = "FilterBottomSheet" }
}
