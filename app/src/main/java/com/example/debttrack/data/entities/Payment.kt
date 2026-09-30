package com.example.debttrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * История платежей по долгу. Экран "Добавить платёж" / блок "История платежей"
 * на экране "Детали долга".
 * Связь: один-ко-многим (один Debt -> много Payment).
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Debt::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    /** Дата платежа, timestamp в мс. */
    val date: Long
)
