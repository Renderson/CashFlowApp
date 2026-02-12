package com.renderson.cashflowapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.renderson.cashflowapp.enums.RecurringFrequency
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "userId")
    val userId: String = "",
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,
    @ColumnInfo(name = "frequency")
    val frequency: String,
    @ColumnInfo(name = "next_occurrence")
    val nextOccurrence: String,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

data class RecurringTransaction(
    val id: Int = 0,
    val description: String,
    val type: TypeExtract,
    val category: TransactionCategory,
    val amount: Double,
    val startDate: String,
    val endDate: String?,
    val frequency: RecurringFrequency,
    val nextOccurrence: String,
    val isActive: Boolean
)
