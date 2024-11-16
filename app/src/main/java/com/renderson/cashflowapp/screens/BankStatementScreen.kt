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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun BankStatementScreen(
    viewModel: CashFlowViewModel,
) {
    var selectedMonth by remember { mutableStateOf(viewModel.extract.value?.years?.last()?.months?.last()) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(viewModel.extract.value?.years?.flatMap { it.months }.orEmpty()) { month ->
                MonthTab(month.month, isSelected = month == selectedMonth) {
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

                var total = 0.0
                var deposit = 0.0
                var payment = 0.0
                selectedMonth?.transactions?.forEach {
                    if (it.type == TypeExtract.DEPOSIT) deposit += it.amount
                    if (it.type == TypeExtract.PAYMENT) payment -= it.amount
                    total = deposit - payment
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
            items(selectedMonth?.transactions.orEmpty()) { transaction ->
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
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text(
                text = transaction.date.formatDate(),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = transaction.description,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column {
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
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun PreviewBankStatementScreen() {
    val data = DataExtract(
        years = listOf(
            Years(
                years = "2023",
                months = listOf(
                    Months(
                        month = "2023-06-15",
                        transaction = listOf(
                            Transaction(
                                date = "2023-06-15",
                                description = "Mp *Revanche 1/2",
                                amount = 1.000
                            ),
                            Transaction(
                                date = "2023-06-20",
                                description = "Mp *Revanche 1/2",
                                amount = 100.0
                            )
                        )
                    ),
                    Months(
                        month = "2023-07-23",
                        transaction = listOf(
                            Transaction(
                                date = "2023-07-23",
                                description = "Mp *Revanche 1/2",
                                amount = 100.0
                            )
                        )
                    ),
                    Months(
                        month = "2023-08-15",
                        transaction = listOf(
                            Transaction(
                                date = "2023-08-15",
                                description = "Mp *C&A",
                                amount = 5000.0
                            )
                        )
                    )
                )
            ),
            Years(
                years = "2024",
                months = listOf(
                    Months(
                        month = "2024-08-15",
                        transaction = listOf(
                            Transaction(
                                date = "2024-08-15",
                                description = "Mp *Revanche 1/2",
                                amount = 100.0
                            ),
                            Transaction(
                                date = "2024-08-20",
                                description = "Mp *Revanche 1/2",
                                amount = 1000.0
                            )
                        )
                    )
                )
            )
        )
    )

    BankStatementScreen(
        extract = data
    )
}*/
