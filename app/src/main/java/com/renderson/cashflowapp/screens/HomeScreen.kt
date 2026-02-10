package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.FilterPeriod
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.dateStringToMillis
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.extensions.getFirstDayOfCurrentMonth
import com.renderson.cashflowapp.extensions.getLastDayOfCurrentMonth
import com.renderson.cashflowapp.extensions.millisToDateString
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.model.CardItems
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    name: String,
    viewModel: CashFlowViewModel
) {
    val filterPeriod by viewModel.filterPeriod.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState(initial = emptyList())

    val totalDeposit = filteredTransactions
        .filter { it.type == TypeExtract.DEPOSIT }
        .sumOf { it.amount }
    val totalPayment = filteredTransactions
        .filter { it.type == TypeExtract.PAYMENT }
        .sumOf { it.amount }
    val totalInvestment = filteredTransactions
        .filter { it.type == TypeExtract.INVESTMENT }
        .sumOf { it.amount }
    val totalBalance = totalDeposit - totalPayment

    val countDeposits = filteredTransactions.count { it.type == TypeExtract.DEPOSIT }
    val countPayments = filteredTransactions.count { it.type == TypeExtract.PAYMENT }

    val pagamentos = filteredTransactions.filter { it.type == TypeExtract.PAYMENT }
    val totalPorCategoria = pagamentos
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
    val maiorCategoria = totalPorCategoria.maxByOrNull { it.value }
    val topCategories = totalPorCategoria.entries
        .sortedByDescending { it.value }
        .take(3)

    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showActivitiesBottomSheet by remember { mutableStateOf(false) }
    var showInvestmentsBottomSheet by remember { mutableStateOf(false) }

    val investmentTransactions = filteredTransactions.filter { it.type == TypeExtract.INVESTMENT }

    val itemsCards = listOf(
        CardItems(
            title = stringResource(R.string.home_total_in),
            type = TypeExtract.DEPOSIT,
            total = totalDeposit,
            icon = Icons.AutoMirrored.Outlined.CallReceived
        ),
        CardItems(
            title = stringResource(R.string.home_total_out),
            type = TypeExtract.PAYMENT,
            total = totalPayment,
            icon = Icons.AutoMirrored.Outlined.CallMade
        )
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CashFlowAppBar(
                title = name,
                colorViews = MaterialTheme.colorScheme.onSurface,
                iconBackVisible = false,
                showFilterIcon = true,
                onFilterClick = { showFilterBottomSheet = true }
            )
        },
        content = { paddingValues ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(scrollState)
            ) {
                val recentTransactions = filteredTransactions
                    .sortedByDescending { it.date }
                    .take(10)
                CurrentBalanceComposable(balance = totalBalance)
                LazyRow(
                    modifier = Modifier
                        .padding(all = 8.dp)
                ) {
                    items(itemsCards.size) { index ->
                        ItemCardList(itemsCards[index])
                    }
                    item {
                        InvestimentosCard(
                            totalInvestment = totalInvestment,
                            investmentTransactions = investmentTransactions,
                            onClick = { showInvestmentsBottomSheet = true }
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    text = stringResource(
                        R.string.home_spent_by_category,
                        filterPeriod.label()
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 8.dp,
                            end = 8.dp
                        )
                        .background(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        )
                ) {
                    SaldoChart(
                        transactions = filteredTransactions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        PeriodSummaryCard(
                            countDeposits = countDeposits,
                            countPayments = countPayments
                        )
                    }

                    maiorCategoria?.let { (category, total) ->
                        item {
                            MaiorGastoCard(
                                categoryName = category.label(),
                                total = total
                            )
                        }
                    }

                    if (topCategories.isNotEmpty()) {
                        item {
                            CategoriasDestaqueCard(topCategories)
                        }
                    }

                    item {
                        AtividadesRecentesCard(
                            count = recentTransactions.size,
                            onClick = { showActivitiesBottomSheet = true }
                        )
                    }
                }
            }

            if (showFilterBottomSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var customStartDateStr by remember { mutableStateOf(getFirstDayOfCurrentMonth()) }
                var customEndDateStr by remember { mutableStateOf(getLastDayOfCurrentMonth()) }
                var showStartDatePicker by remember { mutableStateOf(false) }
                var showEndDatePicker by remember { mutableStateOf(false) }
                val startDatePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = dateStringToMillis(getFirstDayOfCurrentMonth()),
                    yearRange = IntRange(2020, 2030)
                )
                val endDatePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = dateStringToMillis(getLastDayOfCurrentMonth()),
                    yearRange = IntRange(2020, 2030)
                )

                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { showFilterBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_filter_by_period),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterPeriod.entries.forEach { period ->
                                FilterChip(
                                    selected = period == filterPeriod,
                                    onClick = {
                                        viewModel.setFilterPeriod(period)
                                        if (period != FilterPeriod.CUSTOM) {
                                            showFilterBottomSheet = false
                                        }
                                    },
                                    label = {
                                        Text(text = period.label())
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(visible = filterPeriod == FilterPeriod.CUSTOM) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_start_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = customStartDateStr.toDisplayDate(),
                                        onValueChange = { },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showStartDatePicker = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.CalendarToday,
                                                    contentDescription = stringResource(R.string.home_pick_start_date)
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showStartDatePicker = true }
                                    )
                                }

                                Text(
                                    text = stringResource(R.string.home_end_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = customEndDateStr.toDisplayDate(),
                                        onValueChange = { },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showEndDatePicker = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.CalendarToday,
                                                    contentDescription = stringResource(R.string.home_pick_end_date)
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showEndDatePicker = true }
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.setFilterCustomRange(customStartDateStr, customEndDateStr)
                                        showFilterBottomSheet = false
                                    },
                                    enabled = customStartDateStr.isNotEmpty() &&
                                        customEndDateStr.isNotEmpty() &&
                                        customStartDateStr <= customEndDateStr,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text(stringResource(R.string.home_filter))
                                }
                            }
                        }
                    }
                }

                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    startDatePickerState.selectedDateMillis?.let { millis ->
                                        customStartDateStr = millisToDateString(millis)
                                    }
                                    showStartDatePicker = false
                                }
                            ) {
                                Text(stringResource(R.string.home_ok), color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartDatePicker = false }) {
                                Text(stringResource(R.string.home_cancel), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    ) {
                        DatePicker(state = startDatePickerState)
                    }
                }

                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    endDatePickerState.selectedDateMillis?.let { millis ->
                                        customEndDateStr = millisToDateString(millis)
                                    }
                                    showEndDatePicker = false
                                }
                            ) {
                                Text(stringResource(R.string.home_ok), color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndDatePicker = false }) {
                                Text(stringResource(R.string.home_cancel), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    ) {
                        DatePicker(state = endDatePickerState)
                    }
                }
            }

            if (showActivitiesBottomSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val recentTransactionsSheet = filteredTransactions
                    .sortedByDescending { it.date }
                    .take(10)

                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { showActivitiesBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_recent_activities),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (recentTransactionsSheet.isEmpty()) {
                            Text(
                                text = stringResource(R.string.home_recent_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentTransactionsSheet) { transaction ->
                                    ItemExtractList(transaction)
                                }
                            }
                        }
                    }
                }
            }

            if (showInvestmentsBottomSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val investmentsSheet = filteredTransactions
                    .filter { it.type == TypeExtract.INVESTMENT }
                    .sortedByDescending { it.date }

                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { showInvestmentsBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_investments),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (investmentsSheet.isEmpty()) {
                            Text(
                                text = stringResource(R.string.home_investments_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(investmentsSheet) { transaction ->
                                    ItemExtractList(transaction)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun InvestimentosCard(
    totalInvestment: Double,
    investmentTransactions: List<Transaction>,
    onClick: () -> Unit
) {
    val hasInvestments = investmentTransactions.isNotEmpty()
    Card(
        modifier = Modifier
            .padding(all = 4.dp)
            .width(150.dp)
            .height(100.dp)
            .then(
                if (hasInvestments) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_investments),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = totalInvestment.formatForBrazilianCurrency(),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (hasInvestments) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.home_access),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AtividadesRecentesCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_recent_title),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (count > 0) {
                    Text(
                        text = stringResource(R.string.home_explore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.home_no_movements),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriasDestaqueCard(
    topCategories: List<Map.Entry<TransactionCategory, Double>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_highlight_categories),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            topCategories.forEachIndexed { index, entry ->
                Text(
                    text = "${index + 1}. ${entry.key.label()} – ${entry.value.formatForBrazilianCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun CurrentBalanceComposable(balance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            )
            .background(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
    ) {
        Column {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                text = balance.formatForBrazilianCurrency(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 2.dp,
                        bottom = 32.dp
                    ),
                text = stringResource(R.string.home_cash_value),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ItemCardList(item: CardItems) {
    Card(
        modifier = Modifier
            .padding(all = 4.dp)
            .width(150.dp)
            .height(100.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Row {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(14.dp),
                    imageVector = item.icon,
                    contentDescription = "icon"
                )
            }
            item.total?.let {
                Text(
                    text = item.total.formatForBrazilianCurrency(),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PeriodSummaryCard(
    countDeposits: Int,
    countPayments: Int
) {
    Card(
        modifier = Modifier
            .padding(all = 4.dp)
            .fillMaxWidth()
            .heightIn(min = 110.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_summary_title),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(
                    R.string.home_summary_counts,
                    countDeposits,
                    countPayments
                ),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MaiorGastoCard(
    categoryName: String,
    total: Double
) {
    Card(
        modifier = Modifier
            .padding(all = 4.dp)
            .fillMaxWidth()
            .heightIn(min = 110.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_biggest_expense_title),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(
                    R.string.home_biggest_expense,
                    categoryName,
                    total.formatForBrazilianCurrency()
                ),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
