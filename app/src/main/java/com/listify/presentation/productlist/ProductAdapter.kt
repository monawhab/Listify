package com.listify.presentation.productlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.listify.R
import com.listify.databinding.ItemProductCardBinding
import com.listify.domain.model.Product

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ProductViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvCategory.text = product.category
                tvProductName.text = product.title
                tvProductPrice.text = root.context.getString(R.string.price_format, product.price)
                ratingBar.rating = product.rating.rate.toFloat()
                tvRatingCount.text = "(${product.rating.count})"

                ivProductImage.load(product.imageUrl) {
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                    crossfade(300)
                    transformations(RoundedCornersTransformation(8f))
                }

                root.setOnClickListener { onProductClick(product) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}
