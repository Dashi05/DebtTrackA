package com.example.debttrack.ui.home

import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.DebtSortBy

data class HomeFilterState(
    val query: String = "",
    val type: DebtType? = null,
    val categoryId: Long? = null,
    val status: DebtStatus? = null,
    val sortBy: DebtSortBy = DebtSortBy.DUE_DATE
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || categoryId != null || status != null
}
