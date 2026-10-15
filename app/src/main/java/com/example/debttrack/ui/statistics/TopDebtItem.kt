package com.example.debttrack.ui.statistics

data class TopDebtItem(
    val id: Long,
    val rank: Int,
    val avatarInitials: String,
    val avatarColorHex: String,
    val name: String,
    val amountText: String,
    val amountColorRes: Int
)
