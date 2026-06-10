package com.listify.presentation.productlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.listify.databinding.ItemSkeletonCardBinding

class SkeletonAdapter(private val count: Int = 6) :
    RecyclerView.Adapter<SkeletonAdapter.SkeletonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SkeletonViewHolder(ItemSkeletonCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) = Unit
    override fun getItemCount() = count

    class SkeletonViewHolder(binding: ItemSkeletonCardBinding) :
        RecyclerView.ViewHolder(binding.root)
}
