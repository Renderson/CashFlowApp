package com.renderson.cashflowapp.model

data class MonthlyTrendPoint(
    val label: String,
    val currentValue: Double,
    val previousLabel: String,
    val previousValue: Double
)