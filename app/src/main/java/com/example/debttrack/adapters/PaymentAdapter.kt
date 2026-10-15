package com.example.debttrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.debttrack.databinding.ItemPaymentBinding
import com.example.debttrack.ui.debtdetail.PaymentListItem

class PaymentAdapter : ListAdapter<PaymentListItem, PaymentAdapter.PaymentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentListItem) {
            binding.paymentAmountText.text = item.amountText
            binding.paymentDateText.text = item.dateText
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PaymentListItem>() {
        override fun areItemsTheSame(oldItem: PaymentListItem, newItem: PaymentListItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PaymentListItem, newItem: PaymentListItem) =
            oldItem == newItem
    }
}
