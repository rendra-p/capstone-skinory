package com.capstone.skinory.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstone.skinory.data.remote.response.RoutinesItem
import com.capstone.skinory.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val viewModel: RoutineViewModel,
    private val onDeleteClick: (RoutinesItem) -> Unit
) : ListAdapter<RoutinesItem, NotificationAdapter.NotificationViewHolder>(RoutineItemDiffCallback()) {

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
            routine: RoutinesItem,
            onDeleteClick: () -> Unit
        ) {
            binding.textView.text = routine.applied ?: "Unknown"

            val products = routine.nameProduct?.split(",")?.map { it.trim() } ?: listOf("No Product")
            binding.textView3.text = products.mapIndexed { index, product ->
                "${index + 1}. $product"
            }.joinToString("\n")

            binding.imageButton.setOnClickListener {
                onDeleteClick()
            }
        }
    }
}

class RoutineItemDiffCallback : DiffUtil.ItemCallback<RoutinesItem>() {
    override fun areItemsTheSame(oldItem: RoutinesItem, newItem: RoutinesItem): Boolean {
        return oldItem.idProduct == newItem.idProduct
    }

    override fun areContentsTheSame(oldItem: RoutinesItem, newItem: RoutinesItem): Boolean {
        return oldItem == newItem
    }
}