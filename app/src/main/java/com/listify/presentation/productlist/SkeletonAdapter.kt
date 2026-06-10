package com.listify.presentation.productlist

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.listify.databinding.ItemSkeletonCardBinding

class SkeletonAdapter(private val count: Int = 6) :
    RecyclerView.Adapter<SkeletonAdapter.SkeletonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
        val binding = ItemSkeletonCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SkeletonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) = holder.startShimmer()
    override fun getItemCount() = count

    inner class SkeletonViewHolder(private val binding: ItemSkeletonCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun startShimmer() {
            ObjectAnimator.ofFloat(binding.root, "alpha", 0.4f, 1f).apply {
                duration = 800
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                start()
            }
        }
    }
}
