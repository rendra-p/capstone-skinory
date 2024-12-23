package com.capstone.skinory.ui.notifications.chose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.skinory.R
import com.capstone.skinory.data.remote.response.ProductsItem
import com.capstone.skinory.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (ProductsItem) -> Unit
    ) : ListAdapter<ProductsItem, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductsItem) {
            fun formatProductDisplay(productName: String, maxLength: Int = 100): String {
                return if (productName.length > maxLength) {
                    productName.substring(0, maxLength) + "..."
                } else {
                    productName
                }
            }

            binding.tvProductName.text = formatProductDisplay(product.nameProduct ?: "")

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_baseline_insert_photo_24)
                .into(binding.imgProduct)
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<ProductsItem>() {
        override fun areItemsTheSame(oldItem: ProductsItem, newItem: ProductsItem): Boolean {
            return oldItem.idProduct == newItem.idProduct
        }

        override fun areContentsTheSame(oldItem: ProductsItem, newItem: ProductsItem): Boolean {
            return oldItem == newItem
        }
    }
}