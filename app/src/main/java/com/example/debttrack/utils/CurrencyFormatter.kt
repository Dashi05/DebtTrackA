package com.example.debttrack.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Форматирование сумм в рублях, как на макетах: "12 500 ₽", "-3 500 ₽".
 */
object CurrencyFormatter {

    private val symbols = DecimalFormatSymbols(Locale("ru", "RU")).apply {
        groupingSeparator = ' '
    }

    private val format = DecimalFormat("#,##0", symbols)

    /** "12 500 ₽" */
    fun format(amount: Double): String = "${format.format(amount)} ₽"

    /** Со знаком: "+2 500 ₽" / "-3 500 ₽" — используется для чистого баланса и истории платежей. */
    fun formatSigned(amount: Double): String {
        val sign = if (amount >= 0) "+" else "-"
        return "$sign${format.format(kotlin.math.abs(amount))} ₽"
    }
}
