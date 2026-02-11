package com.renderson.cashflowapp.data.repository

import androidx.room.withTransaction
import com.renderson.cashflowapp.data.ClashFlowDatabase
import com.renderson.cashflowapp.enums.RecurringFrequency
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.BackupTransaction
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.RecurringTransaction
import com.renderson.cashflowapp.model.RecurringTransactionEntity
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity
import com.renderson.cashflowapp.model.Years
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ClashFlowRepository @Inject constructor(private val database: ClashFlowDatabase) {

    private val db = database.dataExtractDao()
    private val recurringDao = database.recurringTransactionDao()

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

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

    fun getRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringDao.insert(recurringTransaction.toEntity())
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringDao.update(recurringTransaction.toEntity())
    }

    suspend fun deleteRecurringTransaction(id: Int) {
        recurringDao.deleteById(id)
    }

    suspend fun generateDueRecurringTransactions(currentDate: String) {
        val dueList = recurringDao.getDueUntil(currentDate)
        if (dueList.isEmpty()) return
        val today = runCatching { LocalDate.parse(currentDate, dateFormatter) }.getOrNull() ?: return
        dueList.forEach { entity ->
            var nextOccurrence = entity.nextOccurrence.toLocalDateOrNull() ?: return@forEach
            val endDate = entity.endDate?.toLocalDateOrNull()
            var updated = entity
            var changed = false
            while (!nextOccurrence.isAfter(today) && updated.isActive) {
                addTransaction(
                    date = nextOccurrence.format(dateFormatter),
                    description = updated.description,
                    type = TypeExtract.valueOf(updated.type),
                    category = TransactionCategory.fromString(updated.category),
                    amount = updated.amount
                )
                changed = true
                val nextDate = RecurringFrequency.fromString(updated.frequency).nextDate(nextOccurrence)
                val shouldDeactivate = endDate?.let { nextDate.isAfter(it) } ?: false
                updated = updated.copy(
                    nextOccurrence = nextDate.format(dateFormatter),
                    isActive = if (shouldDeactivate) false else updated.isActive
                )
                if (shouldDeactivate) break
                nextOccurrence = nextDate
            }
            if (changed) {
                recurringDao.update(updated)
            }
        }
    }

    private fun RecurringTransactionEntity.toDomain(): RecurringTransaction =
        RecurringTransaction(
            id = id,
            description = description,
            type = runCatching { TypeExtract.valueOf(type) }.getOrDefault(TypeExtract.PAYMENT),
            category = TransactionCategory.fromString(category),
            amount = amount,
            startDate = startDate,
            endDate = endDate,
            frequency = RecurringFrequency.fromString(frequency),
            nextOccurrence = nextOccurrence,
            isActive = isActive
        )

    private fun RecurringTransaction.toEntity(): RecurringTransactionEntity =
        RecurringTransactionEntity(
            id = id,
            description = description,
            type = type.name,
            category = category.name,
            amount = amount,
            startDate = startDate,
            endDate = endDate,
            frequency = frequency.name,
            nextOccurrence = nextOccurrence,
            isActive = isActive
        )

    private fun String.toLocalDateOrNull(): LocalDate? =
        runCatching { LocalDate.parse(this, dateFormatter) }.getOrNull()
}