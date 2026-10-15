package com.example.debttrack.data

import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    fun toNightMode(): Int = when (this) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
        SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
