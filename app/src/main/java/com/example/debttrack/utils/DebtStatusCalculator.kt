package com.example.debttrack.utils

import com.example.debttrack.data.entities.DebtStatus

/**
 * Логика вычисления статуса долга (Активный / Частично / Погашен / Просрочено)
 * на основе суммы, выплаченной части и срока возврата.
 * Используется при добавлении платежа (Неделя 10) и в фоновой проверке (Неделя 13).
 */
object DebtStatusCalculator {

    fun calculate(
        amount: Double,
        paidAmount: Double,
        dueDateMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): DebtStatus {
        if (paidAmount >= amount) return DebtStatus.PAID

        val isOverdue = DateFormatter.daysUntil(dueDateMillis, nowMillis) < 0
        if (isOverdue) return DebtStatus.OVERDUE

        return if (paidAmount > 0) DebtStatus.PARTIAL else DebtStatus.ACTIVE
    }
}
