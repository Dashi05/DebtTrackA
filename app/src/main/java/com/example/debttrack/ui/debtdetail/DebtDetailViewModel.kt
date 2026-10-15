package com.example.debttrack.ui.debtdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.CategoryRepository
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.utils.DateFormatter
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DebtDetailViewModel(
    private val debtId: Long,
    private val debtRepository: DebtRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtDetailUiState(debtId = debtId))
    val uiState = _uiState

    init {
        viewModelScope.launch {
            combine(
                debtRepository.getWithPayments(debtId),
                categoryRepository.getAll()
            ) { debtWithPayments, categories ->
                val debt = debtWithPayments.debt
                val category = categories.find { it.id == debt.categoryId }
                val remaining = debt.amount - debt.paidAmount

                val payments = debtWithPayments.payments
                    .sortedByDescending { it.date }
                    .map {
                        PaymentListItem(
                            id = it.id,
                            amountText = CurrencyFormatter.formatSigned(it.amount),
                            dateText = DateFormatter.formatDate(it.date)
                        )
                    }

                val progressPercent = if (debt.amount > 0) {
                    ((debt.paidAmount / debt.amount) * 100).toInt().coerceIn(0, 100)
                } else 0

                DebtDetailUiState(
                    isLoading = false,
                    debtId = debt.id,
                    debtType = debt.type,
                    personName = debt.personName,
                    statusText = statusLabel(debt.status),
                    remainingAmountText = CurrencyFormatter.format(remaining),
                    totalAmountText = "из ${CurrencyFormatter.format(debt.amount)}",
                    progressPercent = progressPercent,
                    whoOwesText = if (debt.type == DebtType.OWED_TO_ME) "Мне должны" else "Я должен",
                    createdDateText = DateFormatter.formatDate(debt.createdDate),
                    dueDateText = DateFormatter.formatDate(debt.dueDate),
                    daysLeftText = DateFormatter.formatDaysLeft(debt.dueDate),
                    categoryText = category?.name ?: "Без категории",
                    descriptionText = debt.description.ifBlank { "Без описания" },
                    payments = payments
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    suspend fun deleteDebt() {
        val debt = debtRepository.getById(debtId) ?: return
        debtRepository.deleteDebt(debt)
    }

    private fun statusLabel(status: DebtStatus): String = when (status) {
        DebtStatus.ACTIVE -> "Активный"
        DebtStatus.PARTIAL -> "Частично"
        DebtStatus.PAID -> "Погашен"
        DebtStatus.OVERDUE -> "Просрочено"
    }

    companion object {
        fun factory(debtId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                DebtDetailViewModel(debtId, container.debtRepository, container.categoryRepository)
            }
        }
    }
}
