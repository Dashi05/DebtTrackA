package com.example.debttrack.data.database

import androidx.room.*
import com.example.debttrack.data.entities.Debt
import com.example.debttrack.data.entities.DebtStatus
import com.example.debttrack.data.entities.DebtType
import com.example.debttrack.data.entities.DebtWithPayments
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    // ---------- CRUD (Недели 7-9) ----------

    @Query("SELECT * FROM debts")
    suspend fun getAllOnce(): List<Debt>

    @Query("DELETE FROM debts")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKeepingIds(items: List<Debt>)

    @Insert
    suspend fun insert(debt: Debt): Long

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): Debt?

    @Transaction
    @Query("SELECT * FROM debts WHERE id = :id")
    fun getWithPayments(id: Long): Flow<DebtWithPayments>

    // ---------- Главный экран (Недели 5-6): список, поиск, фильтр, сортировка ----------

    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAll(): Flow<List<Debt>>

    @Query(
        """
        SELECT * FROM debts
        WHERE (:query IS NULL OR personName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
          AND (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:status IS NULL OR status = :status)
        ORDER BY
          CASE WHEN :sortBy = 'DUE_DATE' THEN dueDate END ASC,
          CASE WHEN :sortBy = 'AMOUNT' THEN amount END DESC,
          CASE WHEN :sortBy = 'NAME' THEN personName END ASC
        """
    )
    fun search(
        query: String?,
        type: DebtType?,
        categoryId: Long?,
        status: DebtStatus?,
        sortBy: String
    ): Flow<List<Debt>>

    // ---------- Статистика (Неделя 11) ----------

    @Query("SELECT COALESCE(SUM(amount - paidAmount), 0) FROM debts WHERE type = :type")
    fun getTotalRemaining(type: DebtType): Flow<Double>

    @Query("SELECT COALESCE(SUM(paidAmount), 0) FROM debts")
    fun getTotalPaid(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM debts")
    fun getTotalAmount(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount - paidAmount), 0) FROM debts WHERE status = 'OVERDUE'")
    fun getTotalOverdue(): Flow<Double>

    @Query("SELECT * FROM debts ORDER BY amount DESC LIMIT :limit")
    fun getTopDebts(limit: Int = 5): Flow<List<Debt>>

    // ---------- Изюминка (Неделя 13): долги с приближающимся сроком ----------

    @Query(
        """
        SELECT * FROM debts
        WHERE status != 'PAID'
          AND dueDate BETWEEN :fromMillis AND :toMillis
        """
    )
    suspend fun getDebtsDueBetween(fromMillis: Long, toMillis: Long): List<Debt>
}
