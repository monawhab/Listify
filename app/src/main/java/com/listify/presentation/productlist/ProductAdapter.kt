package com.listify.presentation.productlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.listify.R
import com.listify.databinding.ItemLoadingFooterBinding
import com.listify.databinding.ItemProductCardBinding
import com.listify.domain.model.Product

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<ProductAdapter.Item, RecyclerView.ViewHolder>(DiffCallback) {

    sealed class Item {
        data class ProductItem(val product: Product) : Item()
        object LoadingFooter : Item()
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Item>() {
        private const val VIEW_PRODUCT = 0
        private const val VIEW_LOADING = 1

        override fun areItemsTheSame(old: Item, new: Item): Boolean = when {
            old is Item.ProductItem && new is Item.ProductItem -> old.product.id == new.product.id
            old is Item.LoadingFooter && new is Item.LoadingFooter -> true
            else -> false
        }

        override fun areContentsTheSame(old: Item, new: Item) = old == new
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position) is Item.LoadingFooter) VIEW_LOADING else VIEW_PRODUCT

    fun submitProducts(products: List<Product>, showLoading: Boolean) {
        val items = products.map { Item.ProductItem(it) }.toMutableList<Item>()
        if (showLoading) items.add(Item.LoadingFooter)
        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_LOADING) {
            val binding = ItemLoadingFooterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            LoadingViewHolder(binding)
        } else {
            val binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ProductViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Item.ProductItem -> (holder as ProductViewHolder).bind(item.product)
            is Item.LoadingFooter -> { /* spinner is always visible */ }
        }
    }

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

    inner class LoadingViewHolder(binding: ItemLoadingFooterBinding) :
        RecyclerView.ViewHolder(binding.root)
}
