package com.example.debttrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Промежуточная (junction) таблица для связи многие-ко-многим Debt <-> Tag.
 * Один долг может иметь несколько меток, одна метка может относиться к нескольким долгам.
 */
@Entity(
    tableName = "debt_tag_cross_ref",
    primaryKeys = ["debtId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Debt::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId"), Index("tagId")]
)
data class DebtTagCrossRef(
    val debtId: Long,
    val tagId: Long
)
