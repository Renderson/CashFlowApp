package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.RecurringFrequency
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.dateStringToMillis
import com.renderson.cashflowapp.extensions.formatForLocalCurrency
import com.renderson.cashflowapp.extensions.getTodayAsString
import com.renderson.cashflowapp.extensions.millisToDateString
import com.renderson.cashflowapp.extensions.parseCurrencyToDouble
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.model.RecurringTransaction
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.util.components.CashFlowTextField
import com.renderson.cashflowapp.util.components.TypeInputEnum
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun RecurringTransactionsScreen(
    onBack: () -> Unit,
    viewModel: CashFlowViewModel
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<RecurringTransaction?>(null) }
    var deleteTarget by remember { mutableStateOf<RecurringTransaction?>(null) }

    Scaffold(
        topBar = {
            CashFlowAppBar(
                title = stringResource(R.string.recurring_title),
                colorViews = MaterialTheme.colorScheme.onSurface,
                onIconBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTransaction = null
                showDialog = true
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        if (recurringTransactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.recurring_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recurringTransactions, key = { it.id }) { recurring ->
                    RecurringTransactionItem(
                        recurringTransaction = recurring,
                        onEdit = {
                            editingTransaction = recurring
                            showDialog = true
                        },
                        onDelete = { deleteTarget = recurring }
                    )
                }
            }
        }
    }

    if (showDialog) {
        RecurringTransactionDialog(
            initial = editingTransaction,
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.saveRecurringTransaction(it)
                showDialog = false
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.recurring_delete_title)) },
            text = { Text(stringResource(R.string.recurring_delete_message, target.description)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecurringTransaction(target.id)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.home_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.home_cancel))
                }
            }
        )
    }
}

@Composable
private fun RecurringTransactionItem(
    recurringTransaction: RecurringTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = recurringTransaction.description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.recurring_amount_type,
                    recurringTransaction.amount.formatForLocalCurrency(),
                    stringResource(recurringTransaction.type.labelRes)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.recurring_next_occurrence,
                    recurringTransaction.nextOccurrence.toDisplayDate()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(R.string.recurring_edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.recurring_delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringTransactionDialog(
    initial: RecurringTransaction?,
    onDismiss: () -> Unit,
    onConfirm: (RecurringTransaction) -> Unit
) {
    val today = getTodayAsString()
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var amountText by remember { mutableStateOf(initial?.amount?.formatForLocalCurrency().orEmpty()) }
    var type by remember { mutableStateOf(initial?.type ?: TypeExtract.PAYMENT) }
    var category by remember { mutableStateOf(initial?.category ?: TransactionCategory.OUTROS) }
    var frequency by remember { mutableStateOf(initial?.frequency ?: RecurringFrequency.MONTHLY) }
    var startDate by remember { mutableStateOf(initial?.startDate ?: today) }
    var endDate by remember { mutableStateOf(initial?.endDate.orEmpty()) }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.recurring_add else R.string.recurring_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.recurring_field_description)) },
                    modifier = Modifier.fillMaxWidth()
                )
                CashFlowTextField(
                    value = amountText,
                    hint = stringResource(R.string.recurring_field_amount),
                    onInputChange = { amountText = it },
                    inputType = TypeInputEnum.CURRENCY,
                    modifier = Modifier.fillMaxWidth()
                )
                EnumDropdownField(
                    label = stringResource(R.string.recurring_field_type),
                    valueText = stringResource(type.labelRes),
                    options = TypeExtract.values().toList(),
                    labelResProvider = { it.labelRes },
                    onSelect = { type = it }
                )
                EnumDropdownField(
                    label = stringResource(R.string.recurring_field_category),
                    valueText = stringResource(category.displayNameRes),
                    options = TransactionCategory.values().toList(),
                    labelResProvider = { it.displayNameRes },
                    onSelect = { category = it }
                )
                EnumDropdownField(
                    label = stringResource(R.string.recurring_field_frequency),
                    valueText = when (frequency) {
                        RecurringFrequency.WEEKLY -> stringResource(R.string.recurring_frequency_weekly)
                        RecurringFrequency.MONTHLY -> stringResource(R.string.recurring_frequency_monthly)
                        RecurringFrequency.YEARLY -> stringResource(R.string.recurring_frequency_yearly)
                    },
                    options = RecurringFrequency.values().toList(),
                    labelResProvider = {
                        when (it) {
                            RecurringFrequency.WEEKLY -> R.string.recurring_frequency_weekly
                            RecurringFrequency.MONTHLY -> R.string.recurring_frequency_monthly
                            RecurringFrequency.YEARLY -> R.string.recurring_frequency_yearly
                        }
                    },
                    onSelect = { frequency = it }
                )
                DateField(
                    label = stringResource(R.string.recurring_field_start_date),
                    date = startDate,
                    onClick = { showStartPicker = true }
                )
                DateField(
                    label = stringResource(R.string.recurring_field_end_date),
                    date = endDate,
                    onClick = { showEndPicker = true },
                    placeholder = stringResource(R.string.recurring_field_end_date_hint)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.recurring_field_active))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                if (showValidationError) {
                    Text(
                        text = stringResource(R.string.recurring_validation_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.parseCurrencyToDouble()
                if (description.isBlank() || amount <= 0.0) {
                    showValidationError = true
                    return@TextButton
                }
                val nextOccurrence = if (initial == null || startDate != initial.startDate) startDate else initial.nextOccurrence
                onConfirm(
                    RecurringTransaction(
                        id = initial?.id ?: 0,
                        description = description,
                        type = type,
                        category = category,
                        amount = amount,
                        startDate = startDate,
                        endDate = endDate.ifBlank { null },
                        frequency = frequency,
                        nextOccurrence = nextOccurrence,
                        isActive = isActive
                    )
                )
            }) {
                Text(stringResource(R.string.home_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_cancel))
            }
        }
    )

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateStringToMillis(startDate))
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        startDate = millisToDateString(it)
                    }
                    showStartPicker = false
                }) {
                    Text(stringResource(R.string.home_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.home_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val initialMillis = endDate.takeIf { it.isNotBlank() }?.let { dateStringToMillis(it) }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        endDate = millisToDateString(it)
                    }
                    showEndPicker = false
                }) {
                    Text(stringResource(R.string.home_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.home_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun <T> EnumDropdownField(
    label: String,
    valueText: String,
    options: List<T>,
    labelResProvider: (T) -> Int,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Text(text = valueText, color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelResProvider(option))) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: String,
    onClick: () -> Unit,
    placeholder: String? = null
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                .clickable { onClick() }
                .padding(12.dp)
        ) {
            if (date.isBlank()) {
                Text(
                    text = placeholder ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = date.toDisplayDate(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
