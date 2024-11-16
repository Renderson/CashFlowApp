package com.renderson.cashflowapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.renderson.cashflowapp.data.relations.MonthWithTransactions
import com.renderson.cashflowapp.data.relations.YearWithMonths
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@Dao
interface DataExtractDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(yearEntity: YearEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonth(monthEntity: MonthEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM months WHERE yearId = :yearId")
    suspend fun getMonthsByYearId(yearId: Int): List<MonthEntity>

    @Query("SELECT * FROM transactions WHERE monthId = :monthId")
    suspend fun getTransactionsByMonthId(monthId: Int): List<TransactionEntity>

    @Query("SELECT * FROM years")
    fun getAllYears(): Flow<List<YearWithMonths>>

    @Transaction
    @Query(
        """
    SELECT * FROM months
    WHERE yearId = :yearId"""
    )
    suspend fun getMonthsWithTransactionsForYear(yearId: Int): List<MonthWithTransactions> {
        val months = getMonthsByYearId(yearId)
        return months.map { month ->
            val transactions = getTransactionsByMonthId(month.monthId)
            MonthWithTransactions(month, transactions)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Transaction
    @Query("SELECT * FROM years")
    fun getAllData(): Flow<List<YearWithMonths>> {
        return getAllYears().flatMapLatest { years ->
            flow {
                val yearWithMonthsList = years.map { yearEntity ->
                    val monthsWithTransactions =
                        getMonthsWithTransactionsForYear(yearEntity.year.yearId)
                    YearWithMonths(yearEntity.year, monthsWithTransactions)
                }
                emit(yearWithMonthsList)
            }
        }
    }
}
