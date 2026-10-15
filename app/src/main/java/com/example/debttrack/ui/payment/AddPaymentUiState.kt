package com.example.debttrack.ui.payment

data class AddPaymentUiState(
    val isLoading: Boolean = true,
    val personName: String = "",
    val remainingAmount: Double = 0.0,
    val remainingAmountText: String = ""
)
