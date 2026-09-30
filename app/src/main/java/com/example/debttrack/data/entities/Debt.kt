package com.example.debttrack.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Основная таблица долгов. Соответствует экранам "Новый долг" / "Детали долга".
 * Связь с Category: многие-к-одному (many Debt -> one Category), CASCADE при удалении категории.
 * Связь с Payment: один-ко-многим (Debt -> Payment), см. Payment.kt.
 */
@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val personName: String,

    val type: DebtType,

    /** Общая сумма долга. */
    val amount: Double,

    /** Сумма уже выплаченного (сумма связанных Payment), кэшируется здесь для быстрого отображения списка. */
    val paidAmount: Double = 0.0,

    val status: DebtStatus = DebtStatus.ACTIVE,

    @ColumnInfo(name = "categoryId")
    val categoryId: Long,

    val description: String = "",

    /** Дата создания долга, timestamp в мс. */
    val createdDate: Long,

    /** Срок возврата, timestamp в мс. */
    val dueDate: Long
)
