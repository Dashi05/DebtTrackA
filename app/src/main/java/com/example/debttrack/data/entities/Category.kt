package com.example.debttrack.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Категория долга (Электроника, Путешествия, Автомобиль, ...).
 * Один-ко-многим: одна категория -> много долгов.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#3B6FE0"
)
