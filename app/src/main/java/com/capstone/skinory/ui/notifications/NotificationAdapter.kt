package com.capstone.skinory.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstone.skinory.data.remote.response.GroupedRoutinesItem
import com.capstone.skinory.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val onDeleteClick: (GroupedRoutinesItem) -> Unit
) : ListAdapter<GroupedRoutinesItem, NotificationAdapter.NotificationViewHolder>(RoutineItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val routine = getItem(position)
        holder.bind(
            routine,
            onDeleteClick = { onDeleteClick(routine) }
        )
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            routine: GroupedRoutinesItem,
            onDeleteClick: () -> Unit
        ) {
            binding.tvUsage.text = routine.applied

            fun formatProductDisplay(productName: String, maxLength: Int = 50): String {
                return if (productName.length > maxLength) {
                    productName.substring(0, maxLength) + "..."
                } else {
                    productName
                }
            }

            val displayProducts = routine.products.mapIndexed { index, product ->
                "${index + 1}. ${formatProductDisplay(product)}"
            }.joinToString("\n")

            binding.tvNotificationProduct.text = displayProducts

            binding.imgbtnDelete.setOnClickListener {
                onDeleteClick()
            }
        }
    }
}

class RoutineItemDiffCallback : DiffUtil.ItemCallback<GroupedRoutinesItem>() {
    override fun areItemsTheSame(oldItem: GroupedRoutinesItem, newItem: GroupedRoutinesItem): Boolean {
        return oldItem.applied == newItem.applied
    }

    override fun areContentsTheSame(oldItem: GroupedRoutinesItem, newItem: GroupedRoutinesItem): Boolean {
        return oldItem == newItem
    }
}