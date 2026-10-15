package com.example.debttrack.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.data.repository.PaymentRepository
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddPaymentViewModel(
    private val debtId: Long,
    private val debtRepository: DebtRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPaymentUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            val debt = debtRepository.getById(debtId)
            if (debt != null) {
                val remaining = debt.amount - debt.paidAmount
                _uiState.value = AddPaymentUiState(
                    isLoading = false,
                    personName = debt.personName,
                    remainingAmount = remaining,
                    remainingAmountText = CurrencyFormatter.format(remaining)
                )
            }
        }
    }

    /**
     * Добавляет платёж и пересчитывает paidAmount/status долга (логика — в PaymentRepository).
     * Возвращает true при успехе.
     */
    suspend fun submitPayment(amount: Double): Boolean {
        return try {
            paymentRepository.addPayment(
                debtId = debtId,
                amount = amount,
                dateMillis = System.currentTimeMillis()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        fun factory(debtId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                AddPaymentViewModel(debtId, container.debtRepository, container.paymentRepository)
            }
        }
    }
}
