package com.example.debttrack.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.debttrack.databinding.ItemTopDebtBinding
import com.example.debttrack.ui.statistics.TopDebtItem

class TopDebtAdapter : ListAdapter<TopDebtItem, TopDebtAdapter.TopDebtViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopDebtViewHolder {
        val binding = ItemTopDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopDebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopDebtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TopDebtViewHolder(private val binding: ItemTopDebtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopDebtItem) {
            val context = binding.root.context

            binding.rankText.text = item.rank.toString()

            binding.avatarText.text = item.avatarInitials
            (binding.avatarText.background.mutate() as? GradientDrawable)?.setColor(
                Color.parseColor(item.avatarColorHex)
            )

            binding.nameText.text = item.name
            binding.amountText.text = item.amountText
            binding.amountText.setTextColor(ContextCompat.getColor(context, item.amountColorRes))
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TopDebtItem>() {
        override fun areItemsTheSame(oldItem: TopDebtItem, newItem: TopDebtItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TopDebtItem, newItem: TopDebtItem) =
            oldItem == newItem
    }
}
