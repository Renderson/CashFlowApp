package com.renderson.cashflowapp.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.renderson.cashflowapp.enums.TypeExtract

data class Extract(
    val cash: String? = null,
    val description: String? = null,
    val title: String? = null,
    val type: TypeExtract? = null,
    val icon: ImageVector? = null,
    val data: String? = null
)