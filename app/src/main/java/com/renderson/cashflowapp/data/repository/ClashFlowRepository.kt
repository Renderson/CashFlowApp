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