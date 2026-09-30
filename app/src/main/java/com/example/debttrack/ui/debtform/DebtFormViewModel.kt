package com.example.debttrack.ui.debtform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.CategoryRepository
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DebtFormViewModel(
    private val debtRepository: DebtRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Category>())

    /**
     * Создаёт новый долг (Неделя 7). Возвращает id созданной записи.
     * Валидация полей выполняется во фрагменте до вызова этого метода.
     */
    suspend fun createDebt(
        personName: String,
        type: DebtType,
        amount: Double,
        categoryId: Long,
        description: String,
        dueDate: Long
    ): Long = debtRepository.createDebt(
        personName = personName,
        type = type,
        amount = amount,
        categoryId = categoryId,
        description = description,
        dueDate = dueDate
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                DebtFormViewModel(container.debtRepository, container.categoryRepository)
            }
        }
    }
}
