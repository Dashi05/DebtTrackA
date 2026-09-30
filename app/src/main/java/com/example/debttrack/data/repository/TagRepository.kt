package com.example.debttrack.data.repository

import com.example.debttrack.data.database.TagDao
import com.example.debttrack.data.entities.DebtTagCrossRef
import com.example.debttrack.data.entities.DebtWithTags
import com.example.debttrack.data.entities.Tag
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    fun getAll(): Flow<List<Tag>> = tagDao.getAll()

    suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    suspend fun delete(tag: Tag) = tagDao.delete(tag)

    suspend fun addTagToDebt(debtId: Long, tagId: Long) =
        tagDao.addTagToDebt(DebtTagCrossRef(debtId, tagId))

    suspend fun removeTagFromDebt(debtId: Long, tagId: Long) =
        tagDao.removeTagFromDebt(DebtTagCrossRef(debtId, tagId))

    fun getDebtWithTags(debtId: Long): Flow<DebtWithTags> = tagDao.getDebtWithTags(debtId)
}
