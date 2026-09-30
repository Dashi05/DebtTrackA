package com.example.debttrack

import android.app.Application
import com.example.debttrack.data.AppContainer

/**
 * Точка входа приложения. Хранит единственный экземпляр AppContainer
 * (база данных + репозитории), доступный всему приложению через (application as DebtTrackApplication).container.
 */
class DebtTrackApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
