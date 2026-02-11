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

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM recurring_transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions ORDER BY next_occurrence ASC")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE is_active = 1 ORDER BY next_occurrence ASC")
    fun getActive(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE is_active = 1 AND next_occurrence <= :date ORDER BY next_occurrence ASC")
    suspend fun getDueUntil(date: String): List<RecurringTransactionEntity>
}
