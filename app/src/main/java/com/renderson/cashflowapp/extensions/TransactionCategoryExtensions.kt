package com.renderson.cashflowapp.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.renderson.cashflowapp.enums.TransactionCategory

@Composable
fun TransactionCategory.label(): String = stringResource(displayNameRes)
