package com.example.debttrack.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Форматирование дат, как на макетах: "15 сент. 2026", "Осталось 7 дней", "Просрочено".
 */
object DateFormatter {

    private val fullDateFormat = SimpleDateFormat("d MMM yyyy", Locale("ru"))
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale("ru"))

    /** "01 дек. 2025" */
    fun formatDate(millis: Long): String = fullDateFormat.format(millis)

    /** "10 янв." — используется в карточке списка: "Осталось 5 дней (до 10 янв.)" */
    fun formatShortDate(millis: Long): String = shortDateFormat.format(millis)

    /**
     * "Осталось 7 дней" / "Осталось 1 день" / "Просрочено на 3 дня".
     * Склонение слова "день" упрощено под частые случаи из макетов.
     */
    fun formatDaysLeft(dueDateMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diffMillis = dueDateMillis - startOfDay(nowMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            days < 0 -> "Просрочено на ${pluralDays(-days)}"
            days == 0L -> "Срок сегодня"
            else -> "Осталось ${pluralDays(days)}"
        }
    }

    /** Дней до срока (может быть отрицательным, если просрочено). Используется для сортировки/фильтров. */
    fun daysUntil(dueDateMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
        return TimeUnit.MILLISECONDS.toDays(dueDateMillis - startOfDay(nowMillis))
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun pluralDays(days: Long): String {
        val n = days % 100
        val d = n % 10
        val word = when {
            n in 11..14 -> "дней"
            d == 1L -> "день"
            d in 2..4 -> "дня"
            else -> "дней"
        }
        return "$days $word"
    }
}
