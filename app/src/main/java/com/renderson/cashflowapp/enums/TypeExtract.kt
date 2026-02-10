package com.renderson.cashflowapp.enums

import androidx.annotation.StringRes
import com.renderson.cashflowapp.R

enum class TypeExtract(@StringRes val labelRes: Int) {
    DEPOSIT(R.string.type_deposit),
    PAYMENT(R.string.type_payment),
    INVESTMENT(R.string.type_investment)
}