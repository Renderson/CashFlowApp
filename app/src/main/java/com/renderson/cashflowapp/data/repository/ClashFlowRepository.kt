package com.renderson.cashflowapp.data.repository

import androidx.room.withTransaction
import com.renderson.cashflowapp.data.ClashFlowDatabase
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.BackupTransaction
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity
import com.renderson.cashflowapp.model.Years
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClashFlowRepository @Inject constructor(private val database: ClashFlowDatabase) {

    private val db = database.dataExtractDao()

    suspend fun addTransaction(date: String, description: String, type: TypeExtract, category: TransactionCategory, amount: Double) {
        if (date.length < 10) return
        val yearStr = date.take(4)
        val monthStr = "${date.take(7)}-01"
        database.withTransaction {
            var yearEntity = db.getYearByYear(yearStr)
            if (yearEntity == null) {
                db.insertYear(YearEntity(yearId = 0, year = yearStr))
                yearEntity = db.getYearByYear(yearStr) ?: return@withTransaction
            }
            val yearId = yearEntity.yearId
            var monthEntity = db.getMonthByYearIdAndMonth(yearId, monthStr)
            if (monthEntity == null) {
                db.insertMonth(MonthEntity(monthId = 0, month = monthStr, yearId = yearId))
                monthEntity = db.getMonthByYearIdAndMonth(yearId, monthStr) ?: return@withTransaction
            }
            val monthId = monthEntity.monthId
            db.insertTransaction(
                TransactionEntity(
                    transactionId = 0,
                    date = date,
                    description = description,
                    type = type.name,
                    category = category.name,
                    amount = amount,
                    monthId = monthId
                )
            )
        }
    }

    suspend fun updateTransaction(
        transactionId: Int,
        date: String,
        description: String,
        type: TypeExtract,
        category: TransactionCategory,
        amount: Double
    ) {
        if (date.length < 10) return
        database.withTransaction {
            db.deleteTransaction(transactionId)
            val yearStr = date.take(4)
            val monthStr = "${date.take(7)}-01"
            var yearEntity = db.getYearByYear(yearStr)
            if (yearEntity == null) {
                db.insertYear(YearEntity(yearId = 0, year = yearStr))
                yearEntity = db.getYearByYear(yearStr) ?: return@withTransaction
            }
            val yearId = yearEntity.yearId
            var monthEntity = db.getMonthByYearIdAndMonth(yearId, monthStr)
            if (monthEntity == null) {
                db.insertMonth(MonthEntity(monthId = 0, month = monthStr, yearId = yearId))
                monthEntity = db.getMonthByYearIdAndMonth(yearId, monthStr) ?: return@withTransaction
            }
            val monthId = monthEntity.monthId
            db.insertTransaction(
                TransactionEntity(
                    transactionId = 0,
                    date = date,
                    description = description,
                    type = type.name,
                    category = category.name,
                    amount = amount,
                    monthId = monthId
                )
            )
        }
    }

    suspend fun deleteTransaction(transactionId: Int) {
        db.deleteTransaction(transactionId)
    }

    suspend fun getExtractSnapshot(): DataExtract = getExtract().first()

    suspend fun restoreFromBackup(transactions: List<BackupTransaction>) {
        database.withTransaction {
            db.deleteAllTransactions()
            db.deleteAllMonths()
            db.deleteAllYears()
        }
        transactions.forEach { bt ->
            val type = try {
                TypeExtract.valueOf(bt.type)
            } catch (e: IllegalArgumentException) {
                TypeExtract.PAYMENT
            }
            val category = TransactionCategory.fromString(bt.category)
            addTransaction(bt.date, bt.description, type, category, bt.amount)
        }
    }

    fun getExtract(): Flow<DataExtract> {
        return db.getAllData().map { yearsWithMonths ->
            DataExtract(
                years = yearsWithMonths.map { yearWithMonths ->
                    Years(
                        year = yearWithMonths.year.year,
                        months = yearWithMonths.months.map { monthWithTransactions ->
                            Months(
                                month = monthWithTransactions.month.month,
                                transactions = monthWithTransactions.transactions.map { transactionEntity ->
                                    Transaction(
                                        transactionId = transactionEntity.transactionId,
                                        date = transactionEntity.date,
                                        description = transactionEntity.description,
                                        type = TypeExtract.valueOf(transactionEntity.type),
                                        category = TransactionCategory.fromString(transactionEntity.category),
                                        amount = transactionEntity.amount
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    }
}