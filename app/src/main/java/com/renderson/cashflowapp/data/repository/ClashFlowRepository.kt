package com.renderson.cashflowapp.data.repository

import androidx.room.withTransaction
import com.renderson.cashflowapp.data.ClashFlowDatabase
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity
import com.renderson.cashflowapp.model.Years
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClashFlowRepository @Inject constructor(private val database: ClashFlowDatabase) {

    private val db = database.dataExtractDao()

    suspend fun saveExtract(dataExtract: DataExtract) {
        database.withTransaction {
            dataExtract.years.forEach { year ->
                val yearEntity = YearEntity(yearId = 0, year = year.year)
                val yearId = db.insertYear(yearEntity)

                year.months.forEach { month ->
                    val monthEntity = MonthEntity(
                        monthId = 0,
                        month = month.month,
                        yearId = yearId.toInt()
                    )
                    val monthId = db.insertMonth(monthEntity)

                    month.transactions.forEach { transaction ->
                        val transactionEntity = TransactionEntity(
                            transactionId = 0,
                            date = transaction.date,
                            description = transaction.description,
                            type = transaction.type.name,
                            amount = transaction.amount,
                            monthId = monthId.toInt()
                        )
                        db.insertTransaction(transactionEntity)
                    }
                }
            }
        }
    }

    suspend fun addTransaction(date: String, description: String, type: TypeExtract, amount: Double) {
        if (date.length < 10) return
        val yearStr = date.substring(0, 4)
        val monthStr = "${date.substring(0, 7)}-01"
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
                    amount = amount,
                    monthId = monthId
                )
            )
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
                                        date = transactionEntity.date,
                                        description = transactionEntity.description,
                                        type = TypeExtract.valueOf(transactionEntity.type),
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