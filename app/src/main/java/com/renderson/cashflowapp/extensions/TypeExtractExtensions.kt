package com.renderson.cashflowapp.extensions

import com.renderson.cashflowapp.enums.TypeExtract

fun TypeExtract.label(): String = when (this) {
    TypeExtract.DEPOSIT -> "Deposíto"
    TypeExtract.PAYMENT -> "Pagamento"
    TypeExtract.INVESTMENT -> "Investimento"
}

