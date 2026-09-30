package com.example.debttrack.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Метка долга (например "Срочно", "Друзья", "Семья").
 * Связь с Debt: многие-ко-многим через DebtTagCrossRef.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
