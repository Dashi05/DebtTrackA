package com.example.debttrack.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.debttrack.data.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments")
    suspend fun getAllOnce(): List<Payment>

    @Query("DELETE FROM payments")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKeepingIds(items: List<Payment>)

    @Insert
    suspend fun insert(payment: Payment): Long

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    fun getForDebt(debtId: Long): Flow<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE debtId = :debtId")
    suspend fun getTotalPaidForDebt(debtId: Long): Double
}
