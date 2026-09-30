package com.example.debttrack.ui.home

data class DebtListItem(
    val id: Long,
    val avatarInitials: String,
    val avatarColorHex: String,
    val name: String,
    val subtitle: String,
    val amountText: String,
    val amountColorRes: Int,
    val statusText: String,
    val statusColorRes: Int,
    val progressPercent: Int,
    val daysLeftText: String
)
