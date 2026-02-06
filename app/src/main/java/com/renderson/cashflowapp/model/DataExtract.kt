package com.renderson.cashflowapp.model

import com.renderson.cashflowapp.enums.TypeExtract

data class DataExtract(
    val years: List<Years>
)

data class Years(
    val year: String,
    val months: List<Months>
)

data class Months(
    val month: String,
    val transactions: List<Transaction>
)

data class Transaction(
    val transactionId: Int = 0,
    val date: String,
    val description: String,
    val type: TypeExtract,
    val amount: Double
)