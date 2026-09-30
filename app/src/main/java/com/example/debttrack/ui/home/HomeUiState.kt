package com.example.debttrack.ui.home

data class HomeUiState(
    val items: List<DebtListItem> = emptyList(),
    val owedToMeText: String = "0 ₽",
    val iOweText: String = "0 ₽",
    val netBalanceText: String = "0 ₽",
    val netBalanceIsPositive: Boolean = true,
    val hasActiveFilters: Boolean = false,
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
