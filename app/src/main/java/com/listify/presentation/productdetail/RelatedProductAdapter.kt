package com.listify.presentation.productdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.listify.R
import com.listify.databinding.ItemRelatedProductBinding
import com.listify.domain.model.Product

class RelatedProductAdapter(
    private val onClick: (Product) -> Unit
) : ListAdapter<Product, RelatedProductAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRelatedProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemRelatedProductBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Product) {
            b.tvName.text = p.title
            b.tvPrice.text = b.root.context.getString(R.string.price_format, p.price)
            b.ivImage.load(p.imageUrl) { crossfade(true); transformations(RoundedCornersTransformation(8f)) }
            b.root.setOnClickListener { onClick(p) }
        }
    }

    companion object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }
}
