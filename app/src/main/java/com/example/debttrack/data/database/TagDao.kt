package com.example.debttrack.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.debttrack.data.entities.DebtTagCrossRef
import com.example.debttrack.data.entities.DebtWithTags
import com.example.debttrack.data.entities.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags")
    suspend fun getAllOnce(): List<Tag>

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKeepingIds(items: List<Tag>)

    @Query("SELECT * FROM debt_tag_cross_ref")
    suspend fun getAllCrossRefsOnce(): List<DebtTagCrossRef>

    @Query("DELETE FROM debt_tag_cross_ref")
    suspend fun deleteAllCrossRefs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrossRefs(items: List<DebtTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<Tag>>

    // ---------- Junction table: DebtTagCrossRef ----------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToDebt(crossRef: DebtTagCrossRef)

    @Delete
    suspend fun removeTagFromDebt(crossRef: DebtTagCrossRef)

    @Transaction
    @Query("SELECT * FROM debts WHERE id = :debtId")
    fun getDebtWithTags(debtId: Long): Flow<DebtWithTags>
}
