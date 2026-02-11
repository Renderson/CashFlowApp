package com.renderson.cashflowapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.renderson.cashflowapp.data.dao.DataExtractDao
import com.renderson.cashflowapp.data.dao.RecurringTransactionDao
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.RecurringTransactionEntity
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity

@Database(
    entities = [
        YearEntity::class,
        MonthEntity::class,
        TransactionEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ClashFlowDatabase : RoomDatabase() {
    abstract fun dataExtractDao(): DataExtractDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
}