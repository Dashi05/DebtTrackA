package com.example.debttrack.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.R
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatisticsViewModel(debtRepository: DebtRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            combine(
                debtRepository.getTotalIOwe(),
                debtRepository.getTotalOwedToMe(),
                debtRepository.getTotalOverdue(),
                debtRepository.getTotalPaid(),
                debtRepository.getTopDebts(5)
            ) { iOwe, owedToMe, overdue, paid, topDebts ->
                val total = iOwe + owedToMe
                val owedToMePercent = if (total > 0) ((owedToMe / total) * 100).toInt() else 50
                val iOwePercent = 100 - owedToMePercent

                val topDebtItems = topDebts.mapIndexed { index, debt ->
                    val amountColorRes = if (debt.type == DebtType.OWED_TO_ME) {
                        R.color.color_owed_to_me
                    } else {
                        R.color.color_i_owe
                    }
                    TopDebtItem(
                        id = debt.id,
                        rank = index + 1,
                        avatarInitials = initialsOf(debt.personName),
                        avatarColorHex = if (debt.type == DebtType.OWED_TO_ME) "#14B8A6" else "#EF4444",
                        name = debt.personName,
                        amountText = CurrencyFormatter.format(debt.amount),
                        amountColorRes = amountColorRes
                    )
                }

                StatisticsUiState(
                    isLoading = false,
                    iOweText = CurrencyFormatter.format(iOwe),
                    owedToMeText = CurrencyFormatter.format(owedToMe),
                    overdueText = CurrencyFormatter.format(overdue),
                    paidText = CurrencyFormatter.format(paid),
                    owedToMePercent = owedToMePercent,
                    iOwePercent = iOwePercent,
                    hasAnyDebts = total > 0,
                    topDebts = topDebtItems
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun initialsOf(fullName: String): String {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StatisticsViewModel(appContainer().debtRepository)
            }
        }
    }
}
