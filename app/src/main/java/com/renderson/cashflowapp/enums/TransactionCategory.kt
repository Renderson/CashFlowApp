package com.renderson.cashflowapp.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionCategory(val displayName: String, val icon: ImageVector) {
    ALIMENTACAO("Alimentação", Icons.Default.Restaurant),
    CONVENIENCIA("Conveniência", Icons.Default.Store),
    TRANSPORTE("Transporte", Icons.Default.DirectionsCar),
    MORADIA("Moradia", Icons.Default.Home),
    LAZER("Lazer", Icons.Default.TheaterComedy),
    DEPOSITO("Depósito", Icons.Default.AccountBalance),
    CARTAO_CREDITO("Cartão de crédito", Icons.Default.CreditCard),
    OUTROS("Outros", Icons.Default.Category);

    companion object {
        fun fromString(value: String?): TransactionCategory =
            entries.find { it.name == value } ?: OUTROS
    }
}
