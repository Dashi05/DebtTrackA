package com.example.debttrack.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.debttrack.DebtTrackApplication
import com.example.debttrack.utils.CurrencyFormatter
import com.example.debttrack.utils.DateFormatter
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Ежедневная фоновая проверка долгов с приближающимся сроком возврата (Неделя 13, "изюминка").
 * Учитывает настройки пользователя: включены ли напоминания и за сколько дней предупреждать.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as DebtTrackApplication).container
        val preferences = container.userPreferencesRepository

        val remindersEnabled = preferences.remindersEnabled.first()
        if (!remindersEnabled) return Result.success()

        val daysBefore = preferences.reminderDaysBefore.first()

        val now = System.currentTimeMillis()
        val windowEnd = now + TimeUnit.DAYS.toMillis(daysBefore.toLong())

        val dueDebts = container.debtRepository.getDebtsDueBetween(now, windowEnd)

        NotificationHelper.createNotificationChannel(applicationContext)

        dueDebts.forEach { debt ->
            val remaining = debt.amount - debt.paidAmount
            val text = "${DateFormatter.formatDaysLeft(debt.dueDate)} · " +
                "Осталось ${CurrencyFormatter.format(remaining)}"

            NotificationHelper.showDueDateNotification(
                context = applicationContext,
                debtId = debt.id,
                personName = debt.personName,
                contentText = text
            )
        }

        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "due_date_reminder_check"
    }
}
