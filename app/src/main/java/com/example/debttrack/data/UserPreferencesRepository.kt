package com.example.debttrack.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.debttrack.data.repository.DebtSortBy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Настройки пользователя (экран "Настройки", Неделя 12):
 * тема оформления, сортировка долгов по умолчанию, напоминания о сроках.
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_SORT_BY = stringPreferencesKey("default_sort_by")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_DAYS_BEFORE = intPreferencesKey("reminder_days_before")
        val NOTIFICATION_PROMPT_SHOWN = booleanPreferencesKey("notification_prompt_shown")
    }

    /**
     * Настройки из файла DataStore.
     *
     * Ошибка чтения файла не должна ронять приложение или подвешивать запуск:
     * в этом случае отдаём пустые настройки, и приложение работает на
     * значениях по умолчанию.
     */
    private val preferences: Flow<Preferences> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    val themeMode = preferences.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    val defaultSortBy = preferences.map { prefs ->
        prefs[Keys.DEFAULT_SORT_BY]?.let { runCatching { DebtSortBy.valueOf(it) }.getOrNull() }
            ?: DebtSortBy.DUE_DATE
    }

    val remindersEnabled = preferences.map { prefs ->
        prefs[Keys.REMINDERS_ENABLED] ?: true
    }

    val reminderDaysBefore = preferences.map { prefs ->
        prefs[Keys.REMINDER_DAYS_BEFORE] ?: 3
    }

    /** Показывали ли уже объяснение перед системным запросом разрешения на уведомления. */
    val notificationPromptShown = preferences.map { prefs ->
        prefs[Keys.NOTIFICATION_PROMPT_SHOWN] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDefaultSortBy(sortBy: DebtSortBy) {
        context.dataStore.edit { it[Keys.DEFAULT_SORT_BY] = sortBy.name }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setNotificationPromptShown(shown: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_PROMPT_SHOWN] = shown }
    }

    suspend fun setReminderDaysBefore(days: Int) {
        context.dataStore.edit { it[Keys.REMINDER_DAYS_BEFORE] = days.coerceIn(1, 30) }
    }
}
