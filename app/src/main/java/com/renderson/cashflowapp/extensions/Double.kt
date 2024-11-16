package com.renderson.cashflowapp.extensions

import java.text.DecimalFormat
import java.util.Locale

fun Double.formatForBrazilianCurrency(): String {
    val brazilianFormat = DecimalFormat
        .getCurrencyInstance(Locale("pt", "br"))
    return brazilianFormat.format(this)
}