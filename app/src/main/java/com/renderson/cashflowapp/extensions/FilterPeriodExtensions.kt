package com.renderson.cashflowapp.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.FilterPeriod

@Composable
fun FilterPeriod.label(): String = when (this) {
    FilterPeriod.CURRENT_MONTH -> stringResource(R.string.filter_period_current_month)
    FilterPeriod.THREE_MONTHS -> stringResource(R.string.filter_period_three_months)
    FilterPeriod.SIX_MONTHS -> stringResource(R.string.filter_period_six_months)
    FilterPeriod.ONE_YEAR -> stringResource(R.string.filter_period_one_year)
    FilterPeriod.ALL -> stringResource(R.string.filter_period_all)
    FilterPeriod.CUSTOM -> stringResource(R.string.filter_period_custom)
}

