package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatForLocalCurrency
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.extensions.formatMonthYear
import com.renderson.cashflowapp.extensions.getCurrentMonthKey
import com.renderson.cashflowapp.extensions.toDisplayDate
import androidx.compose.ui.res.stringResource
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun BankStatementScreen(
    viewModel: CashFlowViewModel,
    onEditTransaction: (Transaction) -> Unit
) {
    val extract by viewModel.extract.observeAsState()
    val allMonths = extract?.years?.flatMap { it.months }
        ?.sortedBy { it.month }
        .orEmpty()
    val hasAnyTransactions = allMonths.any { it.transactions.isNotEmpty() }
    var selectedMonth by remember { mutableStateOf<Months?>(null) }
    val currentMonthKey = getCurrentMonthKey()
    val lazyRowState = rememberLazyListState()

    LaunchedEffect(extract) {
        selectedMonth = if (allMonths.isEmpty() || !hasAnyTransactions) {
            null
        } else {
            allMonths.find { it.month == selectedMonth?.month }
                ?: allMonths.find { it.month == currentMonthKey }
                ?: allMonths.last()
        }
    }

    LaunchedEffect(allMonths, selectedMonth, hasAnyTransactions) {
        if (hasAnyTransactions && selectedMonth != null) {
            val index = allMonths.indexOfFirst { it.month == selectedMonth?.month }
            if (index >= 0) {
                lazyRowState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasAnyTransactions) {
            LazyRow(
                state = lazyRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                itemsIndexed(allMonths, key = { index, _ -> index }) { _, month ->
                    MonthTab(month.month, isSelected = month.month == selectedMonth?.month) {
                        selectedMonth = month
                    }
                }
            }
        }
        if (hasAnyTransactions && selectedMonth != null) {
            val month = selectedMonth!!
            var deposit = 0.0
            var payment = 0.0
            month.transactions.forEach {
                if (it.type == TypeExtract.DEPOSIT) deposit += it.amount
                if (it.type == TypeExtract.PAYMENT) payment -= it.amount
            }

            if (month.transactions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clip(shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                R.string.statement_header_period,
                                month.month.formatMonthYear()
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.statement_total_deposit,
                                deposit.formatForLocalCurrency()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(
                                R.string.statement_total_payment,
                                payment.formatForLocalCurrency()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val items = month.transactions.sortedBy { it.date }
                    itemsIndexed(items) { index, transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onEditTransaction(transaction) }
                        )
                        if (index < items.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            } else {
                EmptyStatementContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }
        } else {
            EmptyStatementContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyStatementContent(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("empty_ghost.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.statement_empty_period_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.statement_empty_period_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MonthTab(month: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = month.formatMonthYear(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = transaction.category.icon,
                    contentDescription = transaction.category.label(),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = transaction.description,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = transaction.date.toDisplayDate(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val type = transaction.type.label()
            Text(
                text = transaction.amount.formatForLocalCurrency(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = type.lowercase(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
