package com.example.debttrack.data.repository

import com.example.debttrack.data.database.DebtDao
import com.example.debttrack.data.entities.Debt
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.entities.DebtWithPayments
import com.example.debttrack.utils.DebtStatusCalculator
import kotlinx.coroutines.flow.Flow

/** Способ сортировки списка долгов на главном экране (Недели 5-6, Настройки -> "Сортировка долгов"). */
enum class DebtSortBy {
    DUE_DATE, AMOUNT, NAME
}

class DebtRepository(private val debtDao: DebtDao) {

    // ---------- CRUD (Недели 7-9) ----------

    suspend fun createDebt(
        personName: String,
        type: DebtType,
        amount: Double,
        categoryId: Long,
        description: String,
        dueDate: Long
    ): Long {
        val now = System.currentTimeMillis()
        val debt = Debt(
            personName = personName,
            type = type,
            amount = amount,
            paidAmount = 0.0,
            status = DebtStatusCalculator.calculate(amount, 0.0, dueDate, now),
            categoryId = categoryId,
            description = description,
            createdDate = now,
            dueDate = dueDate
        )
        return debtDao.insert(debt)
    }

    suspend fun updateDebt(debt: Debt) {
        val recalculated = debt.copy(
            status = DebtStatusCalculator.calculate(debt.amount, debt.paidAmount, debt.dueDate)
        )
        debtDao.update(recalculated)
    }

    suspend fun deleteDebt(debt: Debt) = debtDao.delete(debt)

    suspend fun getById(id: Long): Debt? = debtDao.getById(id)

    fun getWithPayments(id: Long): Flow<DebtWithPayments> = debtDao.getWithPayments(id)

    // ---------- Главный экран: список / поиск / фильтр / сортировка (Недели 5-6) ----------

    fun getAll(): Flow<List<Debt>> = debtDao.getAll()

    fun search(
        query: String? = null,
        type: DebtType? = null,
        categoryId: Long? = null,
        status: DebtStatus? = null,
        sortBy: DebtSortBy = DebtSortBy.DUE_DATE
    ): Flow<List<Debt>> = debtDao.search(
        query = query?.takeIf { it.isNotBlank() },
        type = type,
        categoryId = categoryId,
        status = status,
        sortBy = sortBy.name
    )

    // ---------- Статистика (Неделя 11) ----------

    fun getTotalOwedToMe(): Flow<Double> = debtDao.getTotalRemaining(DebtType.OWED_TO_ME)
    fun getTotalIOwe(): Flow<Double> = debtDao.getTotalRemaining(DebtType.I_OWE)
    fun getTotalPaid(): Flow<Double> = debtDao.getTotalPaid()
    fun getTotalAmount(): Flow<Double> = debtDao.getTotalAmount()
    fun getTotalOverdue(): Flow<Double> = debtDao.getTotalOverdue()
    fun getTopDebts(limit: Int = 5): Flow<List<Debt>> = debtDao.getTopDebts(limit)

    // ---------- Изюминка: напоминания (Неделя 13) ----------

    suspend fun getDebtsDueBetween(fromMillis: Long, toMillis: Long): List<Debt> =
        debtDao.getDebtsDueBetween(fromMillis, toMillis)
}
