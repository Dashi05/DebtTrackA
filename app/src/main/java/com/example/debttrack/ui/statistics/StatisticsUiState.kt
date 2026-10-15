package com.example.debttrack.ui.statistics

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val iOweText: String = "0 ₽",
    val owedToMeText: String = "0 ₽",
    val overdueText: String = "0 ₽",
    val paidText: String = "0 ₽",
    val owedToMePercent: Int = 50,
    val iOwePercent: Int = 50,
    val hasAnyDebts: Boolean = false,
    val topDebts: List<TopDebtItem> = emptyList()
)
