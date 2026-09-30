package com.example.debttrack.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.debttrack.databinding.ItemDebtBinding
import com.example.debttrack.ui.home.DebtListItem

class DebtListAdapter(
    private val onItemClick: (DebtListItem) -> Unit
) : ListAdapter<DebtListItem, DebtListAdapter.DebtViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val binding = ItemDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DebtViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DebtViewHolder(
        private val binding: ItemDebtBinding,
        private val onItemClick: (DebtListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DebtListItem) {
            val context = binding.root.context

            binding.avatarText.text = item.avatarInitials
            (binding.avatarText.background.mutate() as? GradientDrawable)?.setColor(
                Color.parseColor(item.avatarColorHex)
            )

            binding.nameText.text = item.name
            binding.subtitleText.text = item.subtitle

            binding.amountText.text = item.amountText
            binding.amountText.setTextColor(ContextCompat.getColor(context, item.amountColorRes))

            binding.statusText.text = item.statusText
            (binding.statusText.background.mutate() as? GradientDrawable)?.setColor(
                ContextCompat.getColor(context, item.statusColorRes)
            )

            binding.progressIndicator.setIndicatorColor(
                ContextCompat.getColor(context, item.amountColorRes)
            )
            binding.progressIndicator.progress = item.progressPercent

            binding.daysLeftText.text = item.daysLeftText

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<DebtListItem>() {
        override fun areItemsTheSame(oldItem: DebtListItem, newItem: DebtListItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DebtListItem, newItem: DebtListItem) =
            oldItem == newItem
    }
}
