package com.example.debttrack.data.repository

import androidx.room.withTransaction
import com.example.debttrack.data.database.AppDatabase
import com.example.debttrack.data.database.DebtDao
import com.example.debttrack.data.database.PaymentDao
import com.example.debttrack.data.entities.Payment
import com.example.debttrack.utils.DebtStatusCalculator
import kotlinx.coroutines.flow.Flow

/**
 * Помимо CRUD над Payment, отвечает за пересчёт Debt.paidAmount и Debt.status
 * после каждого платежа (используется на Неделе 10, "Добавить платёж").
 */
class PaymentRepository(
    private val database: AppDatabase,
    private val paymentDao: PaymentDao,
    private val debtDao: DebtDao
) {

    fun getForDebt(debtId: Long): Flow<List<Payment>> = paymentDao.getForDebt(debtId)

    /**
     * Добавляет платёж и атомарно пересчитывает paidAmount/status родительского долга.
     * Возвращает id созданного платежа.
     */
    suspend fun addPayment(debtId: Long, amount: Double, dateMillis: Long): Long {
        return database.withTransaction {
            val paymentId = paymentDao.insert(Payment(debtId = debtId, amount = amount, date = dateMillis))

            val debt = debtDao.getById(debtId) ?: return@withTransaction paymentId
            val newPaidAmount = paymentDao.getTotalPaidForDebt(debtId)
            val newStatus = DebtStatusCalculator.calculate(
                amount = debt.amount,
                paidAmount = newPaidAmount,
                dueDateMillis = debt.dueDate
            )

            debtDao.update(debt.copy(paidAmount = newPaidAmount, status = newStatus))
            paymentId
        }
    }

    suspend fun deletePayment(payment: Payment) {
        database.withTransaction {
            paymentDao.delete(payment)

            val debt = debtDao.getById(payment.debtId) ?: return@withTransaction
            val newPaidAmount = paymentDao.getTotalPaidForDebt(payment.debtId)
            val newStatus = DebtStatusCalculator.calculate(
                amount = debt.amount,
                paidAmount = newPaidAmount,
                dueDateMillis = debt.dueDate
            )

            debtDao.update(debt.copy(paidAmount = newPaidAmount, status = newStatus))
        }
    }
}
