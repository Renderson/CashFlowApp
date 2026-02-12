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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ClashFlowRepository @Inject constructor(
    private val database: ClashFlowDatabase,
    private val authRepository: AuthRepository
) {

    private val db = database.dataExtractDao()
    private val recurringDao = database.recurringTransactionDao()

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun requireUserId(): String? = authRepository.getCurrentUserId()

    suspend fun migrateLegacyDataIfNeeded(userId: String) {
        database.withTransaction {
            db.migrateLegacyYearsToUser(userId)
            recurringDao.migrateLegacyRecurringToUser(userId)
        }
    }

    suspend fun deleteAllDataForUser(userId: String) {
        database.withTransaction {
            db.deleteTransactionsByUserId(userId)
            db.deleteMonthsByUserId(userId)
            db.deleteYearsByUserId(userId)
            recurringDao.deleteAllByUserId(userId)
        }
    }

    suspend fun addTransaction(date: String, description: String, type: TypeExtract, category: TransactionCategory, amount: Double) {
        val userId = requireUserId() ?: return
        if (date.length < 10) return
        val yearStr = date.take(4)
        val monthStr = "${date.take(7)}-01"
        database.withTransaction {
            var yearEntity = db.getYearByYear(yearStr, userId)
            if (yearEntity == null) {
                db.insertYear(YearEntity(yearId = 0, year = yearStr, userId = userId))
                yearEntity = db.getYearByYear(yearStr, userId) ?: return@withTransaction
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
        val userId = requireUserId() ?: return
        if (date.length < 10) return
        database.withTransaction {
            db.deleteTransaction(transactionId)
            val yearStr = date.take(4)
            val monthStr = "${date.take(7)}-01"
            var yearEntity = db.getYearByYear(yearStr, userId)
            if (yearEntity == null) {
                db.insertYear(YearEntity(yearId = 0, year = yearStr, userId = userId))
                yearEntity = db.getYearByYear(yearStr, userId) ?: return@withTransaction
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

    suspend fun getExtractSnapshot(): DataExtract {
        val userId = requireUserId() ?: return DataExtract(emptyList())
        return db.getAllData(userId).map { toDataExtract(it) }.first()
    }

    suspend fun restoreFromBackup(transactions: List<BackupTransaction>) {
        val userId = requireUserId() ?: return
        database.withTransaction {
            db.deleteTransactionsByUserId(userId)
            db.deleteMonthsByUserId(userId)
            db.deleteYearsByUserId(userId)
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

    fun getExtract(): Flow<DataExtract> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(DataExtract(emptyList()))
            else db.getAllData(user.uid).map { toDataExtract(it) }
        }

    private fun toDataExtract(yearsWithMonths: List<com.renderson.cashflowapp.data.relations.YearWithMonths>): DataExtract =
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

    fun getRecurringTransactions(): Flow<List<RecurringTransaction>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else recurringDao.getAll(user.uid).map { list -> list.map { it.toDomain() } }
        }

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val userId = requireUserId() ?: return
        recurringDao.insert(recurringTransaction.toEntity(userId))
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val userId = requireUserId() ?: return
        val entity = recurringTransaction.toEntity(userId)
        recurringDao.update(entity)
    }

    suspend fun deleteRecurringTransaction(id: Int) {
        val userId = requireUserId() ?: return
        recurringDao.deleteById(id, userId)
    }

    suspend fun generateDueRecurringTransactions(currentDate: String) {
        val userId = requireUserId() ?: return
        val dueList = recurringDao.getDueUntil(currentDate, userId)
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

    private fun RecurringTransaction.toEntity(userId: String): RecurringTransactionEntity =
        RecurringTransactionEntity(
            id = id,
            userId = userId,
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