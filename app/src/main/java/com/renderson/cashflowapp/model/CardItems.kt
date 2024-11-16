package com.renderson.cashflowapp.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.renderson.cashflowapp.enums.TypeExtract

data class CardItems(
    val title: String,
    val type: TypeExtract,
    val total: Double? = null,
    val icon: ImageVector
)
