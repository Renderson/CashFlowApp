package com.renderson.cashflowapp.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.formatMonthYear(): String {
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = inputDateFormat.parse(this)

    val outputDateFormat = SimpleDateFormat("MMM yy", Locale.ENGLISH)
    return outputDateFormat.format(date ?: Date()).uppercase()
}

fun String.formatDate(): String {
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = inputDateFormat.parse(this)

    val outputDateFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    return outputDateFormat.format(date ?: Date()).uppercase()
}