package com.renderson.cashflowapp.extensions

import java.text.DecimalFormat
import java.util.Locale

fun String.parseBrazilianCurrencyToDouble(): Double {
    val cleaned = replace("R$", "").replace(" ", "").trim()
    if (cleaned.isBlank()) return 0.0
    val normalized = cleaned.replace(".", "").replace(",", ".")
    return normalized.toDoubleOrNull() ?: 0.0
}

fun Double.formatForBrazilianCurrency(): String {
    val brazilianFormat = DecimalFormat
        .getCurrencyInstance(Locale("pt", "br"))
    return brazilianFormat.format(this)
}