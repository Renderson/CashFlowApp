package com.renderson.cashflowapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.renderson.cashflowapp.model.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert
    suspend fun insert(entity: RecurringTransactionEntity): Long

    @Update
    suspend fun update(entity: RecurringTransactionEntity)

    @Delete
    suspend fun delete(entity: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Int, userId: String)

    @Query("SELECT * FROM recurring_transactions WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getById(id: Int, userId: String): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY next_occurrence ASC")
    fun getAll(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND is_active = 1 ORDER BY next_occurrence ASC")
    fun getActive(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND is_active = 1 AND next_occurrence <= :date ORDER BY next_occurrence ASC")
    suspend fun getDueUntil(date: String, userId: String): List<RecurringTransactionEntity>

    @Query("DELETE FROM recurring_transactions WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    @Query("UPDATE recurring_transactions SET userId = :newUserId WHERE userId = ''")
    suspend fun migrateLegacyRecurringToUser(newUserId: String)
}
