package com.renderson.cashflowapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "years")
data class YearEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "yearId")
    val yearId: Int = 0,
    @ColumnInfo(name = "year")
    val year: String = "",
    @ColumnInfo(name = "userId")
    val userId: String = ""
)

@Entity("months")
data class MonthEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "monthId")
    val monthId: Int = 0,
    @ColumnInfo(name = "month")
    val month: String = "",
    @ColumnInfo(name = "yearId")
    val yearId: Int = 0
)

@Entity("transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transactionId")
    val transactionId: Int = 0,
    @ColumnInfo(name = "date")
    val date: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "type")
    val type: String = "",
    @ColumnInfo(name = "category")
    val category: String = "OUTROS",
    @ColumnInfo(name = "amount")
    val amount: Double = 0.0,
    @ColumnInfo(name = "monthId")
    val monthId: Int = 0
)
