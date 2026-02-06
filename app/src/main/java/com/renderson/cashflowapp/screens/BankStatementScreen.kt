package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatDate
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.extensions.formatMonthYear
import com.renderson.cashflowapp.extensions.getCurrentMonthKey
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun BankStatementScreen(
    viewModel: CashFlowViewModel,
) {
    val extract by viewModel.extract.observeAsState()
    val allMonths = extract?.years?.flatMap { it.months }
        ?.sortedBy { it.month }
        .orEmpty()
    var selectedMonth by remember { mutableStateOf<Months?>(null) }
    val currentMonthKey = getCurrentMonthKey()
    val lazyRowState = rememberLazyListState()

    LaunchedEffect(extract) {
        selectedMonth = if (allMonths.isEmpty()) {
            null
        } else {
            allMonths.find { it.month == selectedMonth?.month }
                ?: allMonths.find { it.month == currentMonthKey }
                ?: allMonths.last()
        }
    }

    LaunchedEffect(allMonths, selectedMonth) {
        if (allMonths.isNotEmpty() && selectedMonth != null) {
            val index = allMonths.indexOfFirst { it.month == selectedMonth?.month }
            if (index >= 0) {
                lazyRowState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                var deposit = 0.0
                var payment = 0.0
                selectedMonth?.transactions?.forEach {
                    if (it.type == TypeExtract.DEPOSIT) deposit += it.amount
                    if (it.type == TypeExtract.PAYMENT) payment -= it.amount
                }
                Text(
                    text = "Movimentos período, ${selectedMonth?.month?.formatMonthYear()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Deposíto ${deposit.formatForBrazilianCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Total Pagamentos ${payment.formatForBrazilianCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val items = selectedMonth?.transactions?.sortedBy { it.date }
            items(items.orEmpty()) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun MonthTab(month: String, isSelected: Boolean, onClick: () -> Unit) {
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
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = transaction.description,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = transaction.date.formatDate(),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            val type = when (transaction.type) {
                TypeExtract.DEPOSIT -> "Deposíto"
                TypeExtract.PAYMENT -> "Pagamento"
                else -> "Investimento"
            }
            Text(
                text = transaction.amount.formatForBrazilianCurrency(),
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
