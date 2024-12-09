package com.capstone.skinory.ui.result

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.skinory.R
import com.capstone.skinory.data.remote.response.BestProductsItem
import com.capstone.skinory.databinding.ItemBestProductBinding

class BestProductAdapter : ListAdapter<BestProductsItem, BestProductAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemBestProductBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("DefaultLocale")
        fun bind(product: BestProductsItem) {
            binding.apply {
                tvProductName.text = product.category ?: "No Category"
                tvName.text = product.nameProduct ?: "Unnamed Product"

                tvPrice.text = try {
                    val price = product.price?.replace(",", "")?.toFloat() ?: 0f
                    "Rp ${String.format("%,.0f", price)}"
                } catch (e: NumberFormatException) {
                    "Price not available"
                }

                tvRating.text = try {
                    val rating = product.rating?.toFloat() ?: 0f
                    String.format("%.1f", rating)
                } catch (e: NumberFormatException) {
                    "No rating"
                }

                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_baseline_insert_photo_24)
                    .into(imgProduct)

                root.setOnClickListener {
                    product.storeUrl?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        itemView.context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBestProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<BestProductsItem>() {
        override fun areItemsTheSame(
            oldItem: BestProductsItem,
            newItem: BestProductsItem
        ): Boolean {
            return oldItem.idProduct == newItem.idProduct
        }

        override fun areContentsTheSame(
            oldItem: BestProductsItem,
            newItem: BestProductsItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}