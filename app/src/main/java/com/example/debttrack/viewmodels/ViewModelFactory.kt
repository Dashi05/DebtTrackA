package com.example.debttrack.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.DebtTrackApplication
import com.example.debttrack.data.AppContainer

/**
 * Достаёт AppContainer из Application через CreationExtras — так ViewModel-ям
 * не нужен Application-контекст напрямую, только репозитории из контейнера.
 */
fun CreationExtras.appContainer(): AppContainer {
    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DebtTrackApplication
    return app.container
}

/**
 * Пример использования на конкретном экране (появится начиная с Недели 5):
 *
 * class HomeViewModel(private val debtRepository: DebtRepository) : ViewModel() {
 *     companion object {
 *         val Factory: ViewModelProvider.Factory = viewModelFactory {
 *             initializer {
 *                 HomeViewModel(appContainer().debtRepository)
 *             }
 *         }
 *     }
 * }
 *
 * И во фрагменте: private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
 */
