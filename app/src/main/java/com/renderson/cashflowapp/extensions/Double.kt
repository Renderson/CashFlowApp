package com.renderson.cashflowapp.extensions

import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Formata o valor conforme o locale do dispositivo (R$ no Brasil, $ nos EUA, etc.).
 */
fun Double.formatForLocalCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(this)
}

/**
 * Converte string de moeda para Double conforme o locale do dispositivo.
 * Suporta formatos brasileiro (1.234,56) e americano (1,234.56).
 */
fun String.parseCurrencyToDouble(): Double {
    val locale = Locale.getDefault()
    val symbols = DecimalFormatSymbols.getInstance(locale)
    val groupingSep = symbols.groupingSeparator.toString()
    val decimalSep = symbols.decimalSeparator.toString()
    val cleaned = filter { it.isDigit() || it.toString() == groupingSep || it.toString() == decimalSep }.trim()
    if (cleaned.isBlank()) return 0.0
    val normalized = cleaned.replace(groupingSep, "").replace(decimalSep, ".")
    return normalized.toDoubleOrNull() ?: 0.0
}