package com.renderson.cashflowapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.renderson.cashflowapp.data.dao.DataExtractDao
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.TransactionEntity
import com.renderson.cashflowapp.model.YearEntity

@Database(
    entities = [YearEntity::class, MonthEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ClashFlowDatabase : RoomDatabase() {
    abstract fun dataExtractDao(): DataExtractDao
}