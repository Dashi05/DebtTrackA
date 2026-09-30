package com.example.debttrack.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** Долг вместе с историей его платежей (для экрана "Детали долга"). */
data class DebtWithPayments(
    @Embedded val debt: Debt,
    @Relation(parentColumn = "id", entityColumn = "debtId")
    val payments: List<Payment>
)

/** Долг вместе с его метками (many-to-many через DebtTagCrossRef). */
data class DebtWithTags(
    @Embedded val debt: Debt,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DebtTagCrossRef::class,
            parentColumn = "debtId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
