package com.example.debttrack.data.entities

/** Тип долга: мне должны (OWED_TO_ME) или я должен (I_OWE). Соответствует табам на экране "Новый долг". */
enum class DebtType {
    OWED_TO_ME,
    I_OWE
}

/**
 * Статус долга. Рассчитывается автоматически на основе paidAmount/amount
 * и dueDate (см. utils/DebtStatusCalculator, Неделя 10).
 */
enum class DebtStatus {
    ACTIVE,     // "Активный" - платежей ещё не было, срок не истёк
    PARTIAL,    // "Частично" - есть хотя бы один платёж, долг не погашен
    PAID,       // "Погашен" - выплачено полностью
    OVERDUE     // "Просрочено" - срок истёк, долг не погашен полностью
}
