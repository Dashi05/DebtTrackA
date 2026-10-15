package com.example.debttrack.ui.debtdetail

import com.example.debttrack.data.entities.DebtType

data class DebtDetailUiState(
    val isLoading: Boolean = true,
    val debtId: Long = -1L,
    val debtType: DebtType = DebtType.OWED_TO_ME,
    val personName: String = "",
    val statusText: String = "",
    val remainingAmountText: String = "",
    val totalAmountText: String = "",
    val progressPercent: Int = 0,
    val whoOwesText: String = "",
    val createdDateText: String = "",
    val dueDateText: String = "",
    val daysLeftText: String = "",
    val categoryText: String = "",
    val descriptionText: String = "",
    val payments: List<PaymentListItem> = emptyList()
)
