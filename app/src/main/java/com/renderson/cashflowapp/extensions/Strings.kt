package com.renderson.cashflowapp.extensions

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

fun getTodayAsString(): String = dateFormatStorage.format(Calendar.getInstance().time)

/** Chave do mês atual no formato yyyy-MM-01 (para buscar dados do mês atual). */
fun getCurrentMonthKey(): String {
    val cal = Calendar.getInstance()
    return "%04d-%02d-01".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
}

fun dateStringToMillis(dateStr: String): Long {
    return try {
        dateFormatStorage.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * Converte millis do DatePicker em string yyyy-MM-dd.
 * O DatePicker retorna meia-noite do dia em UTC; usamos UTC para extrair ano/mês/dia
 * e evitar que dia 1 vire dia 31 do mês anterior em fusos como Brasil (UTC-3).
 */
fun millisToDateString(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

fun String.toDisplayDate(): String {
    return try {
        val date = dateFormatStorage.parse(this)
        dateFormatDisplay.format(date ?: Date())
    } catch (_: Exception) {
        this
    }
}

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