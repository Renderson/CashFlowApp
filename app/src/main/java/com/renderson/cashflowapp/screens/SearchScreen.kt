package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.SearchPeriod
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.filterByQueryAndPeriod
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    val extract by viewModel.extract.observeAsState()
    val allTransactions = extract?.years?.flatMap { it.months }?.flatMap { it.transactions }
        ?: emptyList()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(SearchPeriod.ALL) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    placeholder = { Text(stringResource(R.string.search_hint_transaction)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                            modifier = Modifier.size(20.dp).clickable {
                                onBack()
                            }
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.search_clear),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriod == SearchPeriod.ALL,
                        onClick = { selectedPeriod = SearchPeriod.ALL },
                        label = { Text(stringResource(R.string.search_chip_all)) }
                    )
                    FilterChip(
                        selected = selectedPeriod == SearchPeriod.DAYS_30,
                        onClick = { selectedPeriod = SearchPeriod.DAYS_30 },
                        label = { Text(stringResource(R.string.search_chip_30_days)) }
                    )
                    FilterChip(
                        selected = selectedPeriod == SearchPeriod.DAYS_15,
                        onClick = { selectedPeriod = SearchPeriod.DAYS_15 },
                        label = { Text(stringResource(R.string.search_chip_15_days)) }
                    )
                    FilterChip(
                        selected = selectedPeriod == SearchPeriod.DAYS_7,
                        onClick = { selectedPeriod = SearchPeriod.DAYS_7 },
                        label = { Text(stringResource(R.string.search_chip_7_days)) }
                    )
                }
                val categoryLabels = TransactionCategory.entries.associateWith { it.label() }
                val filteredList = allTransactions.filterByQueryAndPeriod(
                    query = searchQuery,
                    period = selectedPeriod,
                    categoryLabels = categoryLabels
                )
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (allTransactions.isEmpty()) {
                                stringResource(R.string.search_empty_list)
                            } else {
                                stringResource(R.string.search_empty_result)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(filteredList) { index, transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onEditTransaction(transaction) }
                            )
                            if (index < filteredList.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
