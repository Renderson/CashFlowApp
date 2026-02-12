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

    @Query("SELECT * FROM years WHERE year = :year AND userId = :userId LIMIT 1")
    suspend fun getYearByYear(year: String, userId: String): YearEntity?

    @Query("SELECT * FROM months WHERE yearId = :yearId AND month = :month LIMIT 1")
    suspend fun getMonthByYearIdAndMonth(yearId: Int, month: String): MonthEntity?

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE transactionId = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM months")
    suspend fun deleteAllMonths()

    @Query("DELETE FROM years")
    suspend fun deleteAllYears()

    @Query(
        """
        DELETE FROM transactions WHERE monthId IN (
            SELECT monthId FROM months WHERE yearId IN (
                SELECT yearId FROM years WHERE userId = :userId
            )
        )
        """
    )
    suspend fun deleteTransactionsByUserId(userId: String)

    @Query("DELETE FROM months WHERE yearId IN (SELECT yearId FROM years WHERE userId = :userId)")
    suspend fun deleteMonthsByUserId(userId: String)

    @Query("DELETE FROM years WHERE userId = :userId")
    suspend fun deleteYearsByUserId(userId: String)

    @Query("UPDATE years SET userId = :newUserId WHERE userId = ''")
    suspend fun migrateLegacyYearsToUser(newUserId: String)

    @Query("SELECT * FROM years WHERE userId = :userId")
    fun getAllYears(userId: String): Flow<List<YearWithMonths>>

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
    fun getAllData(userId: String): Flow<List<YearWithMonths>> =
        getAllYears(userId).flatMapLatest { years ->
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
