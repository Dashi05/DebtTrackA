package com.example.debttrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.data.entities.Category
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.repository.CategoryRepository
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.data.repository.DebtSortBy
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val debtRepository: DebtRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(HomeFilterState())

    /** Категории для выпадающего списка фильтра (собираются один раз и переиспользуются). */
    val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Category>())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            filterState.flatMapLatest { filters ->
                combine(
                    debtRepository.search(
                        query = filters.query,
                        type = filters.type,
                        categoryId = filters.categoryId,
                        status = filters.status,
                        sortBy = filters.sortBy
                    ),
                    categoryRepository.getAll(),
                    debtRepository.getTotalOwedToMe(),
                    debtRepository.getTotalIOwe()
                ) { debts, categories, owedToMe, iOwe ->
                    val categoriesById = categories.associateBy { it.id }
                    val items = debts.map { debt ->
                        DebtListItemMapper.map(debt, categoriesById[debt.categoryId])
                    }
                    val netBalance = owedToMe - iOwe

                    HomeUiState(
                        items = items,
                        owedToMeText = CurrencyFormatter.format(owedToMe),
                        iOweText = CurrencyFormatter.format(iOwe),
                        netBalanceText = CurrencyFormatter.formatSigned(netBalance),
                        netBalanceIsPositive = netBalance >= 0,
                        hasActiveFilters = filters.isActive,
                        isLoading = false
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onQueryChanged(query: String) {
        filterState.value = filterState.value.copy(query = query)
    }

    fun onTypeFilterChanged(type: DebtType?) {
        filterState.value = filterState.value.copy(type = type)
    }

    fun onCategoryFilterChanged(categoryId: Long?) {
        filterState.value = filterState.value.copy(categoryId = categoryId)
    }

    fun onStatusFilterChanged(status: DebtStatus?) {
        filterState.value = filterState.value.copy(status = status)
    }

    fun onSortChanged(sortBy: DebtSortBy) {
        filterState.value = filterState.value.copy(sortBy = sortBy)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                HomeViewModel(container.debtRepository, container.categoryRepository)
            }
        }
    }
}
