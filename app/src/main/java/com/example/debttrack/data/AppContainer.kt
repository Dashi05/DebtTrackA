package com.example.debttrack.data

import android.content.Context
import com.example.debttrack.data.database.AppDatabase
import com.example.debttrack.data.repository.CategoryRepository
import com.example.debttrack.data.repository.DebtRepository
import com.example.debttrack.data.repository.PaymentRepository
import com.example.debttrack.data.repository.TagRepository

/**
 * Простой ручной DI-контейнер (без Hilt/Dagger — достаточно для масштаба проекта).
 * Создаётся один раз в DebtTrackApplication и передаётся во ViewModelFactory.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.getInstance(context)

    val debtRepository: DebtRepository by lazy {
        DebtRepository(database.debtDao())
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(database, database.paymentDao(), database.debtDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val tagRepository: TagRepository by lazy {
        TagRepository(database.tagDao())
    }
}
