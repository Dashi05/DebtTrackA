package com.example.debttrack.ui.settings

import com.example.debttrack.data.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val remindersEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3
)
