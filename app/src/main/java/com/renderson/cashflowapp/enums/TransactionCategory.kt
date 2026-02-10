package com.renderson.cashflowapp.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector
import com.renderson.cashflowapp.R

enum class TransactionCategory(@StringRes val displayNameRes: Int, val icon: ImageVector) {
    ALIMENTACAO(R.string.category_alimentacao, Icons.Default.Restaurant),
    CONVENIENCIA(R.string.category_conveniencia, Icons.Default.Store),
    TRANSPORTE(R.string.category_transporte, Icons.Default.DirectionsCar),
    MORADIA(R.string.category_moradia, Icons.Default.Home),
    LAZER(R.string.category_lazer, Icons.Default.TheaterComedy),
    DEPOSITO(R.string.category_deposito, Icons.Default.AccountBalance),
    CARTAO_CREDITO(R.string.category_cartao_credito, Icons.Default.CreditCard),
    POUPANCA_COFRINHO(R.string.category_poupanca_cofrinho, Icons.Filled.Savings),
    OUTROS(R.string.category_outros, Icons.Default.Category);

    companion object {
        fun fromString(value: String?): TransactionCategory =
            entries.find { it.name == value } ?: OUTROS
    }
}
