package com.listify.presentation.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.listify.databinding.BottomSheetSortBinding

class SortBottomSheet(
    private val current: SortOrder,
    private val onSelect: (SortOrder) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSortBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val radioMap = mapOf(
            binding.radioDefault   to SortOrder.DEFAULT,
            binding.radioPriceAsc  to SortOrder.PRICE_ASC,
            binding.radioPriceDesc to SortOrder.PRICE_DESC,
            binding.radioTopRated  to SortOrder.TOP_RATED
        )
        radioMap.entries.firstOrNull { it.value == current }?.key?.isChecked = true
        binding.radioGroupSort.setOnCheckedChangeListener { _, id ->
            radioMap.entries.firstOrNull { it.key.id == id }?.let { onSelect(it.value); dismiss() }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
    companion object { const val TAG = "SortBottomSheet" }
}
