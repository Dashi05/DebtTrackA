package com.example.debttrack.data.repository

import com.example.debttrack.data.database.CategoryDao
import com.example.debttrack.data.entities.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun getById(id: Long): Category? = categoryDao.getById(id)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)
}
