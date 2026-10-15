package com.example.debttrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.debttrack.data.ThemeMode
import com.example.debttrack.data.UserPreferencesRepository
import com.example.debttrack.data.backup.BackupRepository
import com.example.debttrack.viewmodels.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    val uiState = combine(
        preferences.themeMode,
        preferences.remindersEnabled,
        preferences.reminderDaysBefore
    ) { theme, remindersEnabled, reminderDays ->
        SettingsUiState(theme, remindersEnabled, reminderDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setRemindersEnabled(enabled) }
    }

    fun setReminderDaysBefore(days: Int) {
        viewModelScope.launch { preferences.setReminderDaysBefore(days) }
    }

    // ---------- Резервное копирование ----------

    /** Готовит содержимое файла резервной копии. */
    suspend fun buildBackup(): String = backupRepository.export()

    /**
     * Восстанавливает данные из файла.
     *
     * @return количество восстановленных долгов
     */
    suspend fun restoreBackup(json: String): Int = backupRepository.import(json)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(container.userPreferencesRepository, container.backupRepository)
            }
        }
    }
}
