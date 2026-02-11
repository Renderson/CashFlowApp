package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.RecurringFrequency
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.dateStringToMillis
import com.renderson.cashflowapp.extensions.defaultStartDateFrom
import com.renderson.cashflowapp.extensions.formatForLocalCurrency
import com.renderson.cashflowapp.extensions.getTodayAsString
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.extensions.millisToDateString
import com.renderson.cashflowapp.extensions.parseCurrencyToDouble
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.util.components.CashFlowTextField
import com.renderson.cashflowapp.util.components.TypeInputEnum
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    viewModel: CashFlowViewModel,
    @StringRes nameRes: Int,
    onClick: () -> Unit
) {
    val transactionToEdit by viewModel.transactionToEdit.collectAsState(initial = null)
    var amount by remember(transactionToEdit?.transactionId) {
        mutableStateOf(transactionToEdit?.amount?.formatForLocalCurrency() ?: "")
    }
    var description by remember(transactionToEdit?.transactionId) {
        mutableStateOf(transactionToEdit?.description ?: "")
    }
    var dateStr by remember(transactionToEdit?.transactionId) {
        mutableStateOf(transactionToEdit?.date ?: getTodayAsString())
    }
    var selectedType by remember(transactionToEdit?.transactionId) {
        mutableStateOf(transactionToEdit?.type)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }
    val selectedCategory by viewModel.selectedCategory.collectAsState(initial = TransactionCategory.OUTROS)
    var isRecurring by remember(transactionToEdit?.transactionId) { mutableStateOf(false) }
    var recurringFrequency by remember(transactionToEdit?.transactionId) {
        mutableStateOf(RecurringFrequency.MONTHLY)
    }
    var recurringStartDate by remember(transactionToEdit?.transactionId) {
        mutableStateOf(RecurringFrequency.MONTHLY.defaultStartDateFrom(dateStr))
    }
    var recurringEndDate by remember(transactionToEdit?.transactionId) { mutableStateOf("") }
    var showRecurringStartPicker by remember { mutableStateOf(false) }
    var showRecurringEndPicker by remember { mutableStateOf(false) }
    var recurringStartManuallyEdited by remember { mutableStateOf(false) }

    LaunchedEffect(description) {
        if (transactionToEdit == null) {
            viewModel.setSelectedCategory(viewModel.suggestCategory(description))
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToMillis(dateStr),
        yearRange = IntRange(2020, 2030)
    )

    val isFormValid = amount.trim().isNotEmpty() &&
            description.trim().isNotEmpty() &&
            dateStr.isNotEmpty() &&
            selectedType != null &&
            amount.parseCurrencyToDouble() > 0

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = millisToDateString(millis)
                            dateStr = newDate
                            if (isRecurring && !recurringStartManuallyEdited) {
                                val nextDefault = recurringFrequency.defaultStartDateFrom(newDate)
                                recurringStartDate = nextDefault
                                if (recurringEndDate.isNotBlank() && recurringEndDate < nextDefault) {
                                    recurringEndDate = nextDefault
                                }
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        stringResource(R.string.register_ok),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        stringResource(R.string.register_cancel),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showRecurringStartPicker) {
        val startState = rememberDatePickerState(
            initialSelectedDateMillis = dateStringToMillis(recurringStartDate),
            yearRange = IntRange(2020, 2030)
        )
        DatePickerDialog(
            onDismissRequest = { showRecurringStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startState.selectedDateMillis?.let { millis ->
                            val newStart = millisToDateString(millis)
                            recurringStartDate = newStart
                            recurringStartManuallyEdited = true
                            if (recurringEndDate.isNotBlank() && recurringEndDate < newStart) {
                                recurringEndDate = newStart
                            }
                        }
                        showRecurringStartPicker = false
                    }
                ) {
                    Text(
                        stringResource(R.string.register_ok),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurringStartPicker = false }) {
                    Text(
                        stringResource(R.string.register_cancel),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        ) {
            DatePicker(state = startState)
        }
    }

    if (showRecurringEndPicker) {
        val minEndDate = dateStringToMillis(recurringStartDate)
        val initialEndMillis = recurringEndDate.takeIf { it.isNotBlank() }?.let { dateStringToMillis(it) } ?: minEndDate
        val endState = rememberDatePickerState(
            initialSelectedDateMillis = initialEndMillis,
            yearRange = IntRange(2020, 2030),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minEndDate
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showRecurringEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endState.selectedDateMillis?.let { millis ->
                            recurringEndDate = millisToDateString(millis)
                        }
                        showRecurringEndPicker = false
                    }
                ) {
                    Text(
                        stringResource(R.string.register_ok),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurringEndPicker = false }) {
                    Text(
                        stringResource(R.string.register_cancel),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        ) {
            DatePicker(state = endState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            CashFlowAppBar(
                title = stringResource(nameRes),
                colorViews = MaterialTheme.colorScheme.onSurface,
                onIconBackClick = {
                    viewModel.clearTransactionToEdit()
                    onClick.invoke()
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.register_type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    SegmentedButton(
                        selected = selectedType == TypeExtract.DEPOSIT,
                        onClick = { selectedType = TypeExtract.DEPOSIT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.register_type_in))
                    }
                    SegmentedButton(
                        selected = selectedType == TypeExtract.PAYMENT,
                        onClick = { selectedType = TypeExtract.PAYMENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.register_type_out))
                    }
                    SegmentedButton(
                        selected = selectedType == TypeExtract.INVESTMENT,
                        onClick = { selectedType = TypeExtract.INVESTMENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.register_type_investment))
                    }
                }

                if (transactionToEdit != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.register_category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AssistChip(
                        onClick = { showCategorySelector = !showCategorySelector },
                        label = { Text(selectedCategory.label()) },
                        leadingIcon = {
                            Icon(
                                imageVector = selectedCategory.icon,
                                contentDescription = null,
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    )

                    AnimatedVisibility(showCategorySelector) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransactionCategory.entries.forEach { category ->
                                AssistChip(
                                    onClick = {
                                        viewModel.setSelectedCategory(category)
                                        showCategorySelector = false
                                    },
                                    label = { Text(category.label()) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    hint = stringResource(R.string.register_amount_hint),
                    inputType = TypeInputEnum.CURRENCY,
                    keyboardType = KeyboardType.Decimal,
                    maxLength = 16,
                    onInputChange = { amount = it }
                )

                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    hint = stringResource(R.string.register_description_hint),
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    maxLength = 120,
                    onInputChange = { description = it }
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.register_date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateStr.toDisplayDate(),
                    onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = stringResource(R.string.register_pick_date)
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
                            .clickable {
                                isRecurring = false
                                showDatePicker = true
                            }
                    )
                }

                if (transactionToEdit == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.register_recurring_toggle_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.register_recurring_helper_text),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { checked ->
                                    isRecurring = checked
                                    if (checked) {
                                        recurringStartManuallyEdited = false
                                        recurringStartDate =
                                            recurringFrequency.defaultStartDateFrom(dateStr)
                                        recurringEndDate = ""
                                    } else {
                                        recurringStartManuallyEdited = false
                                    }
                                }
                            )
                        }

                        AnimatedVisibility(isRecurring) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.register_recurring_frequency),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RecurringFrequency.values().forEachIndexed { index, freq ->
                                        val labelRes = when (freq) {
                                            RecurringFrequency.WEEKLY -> R.string.recurring_frequency_weekly
                                            RecurringFrequency.MONTHLY -> R.string.recurring_frequency_monthly
                                            RecurringFrequency.YEARLY -> R.string.recurring_frequency_yearly
                                        }
                                        SegmentedButton(
                                            selected = recurringFrequency == freq,
                                            onClick = {
                                                recurringFrequency = freq
                                                recurringEndDate = ""
                                                if (isRecurring) {
                                                    recurringStartDate = freq.defaultStartDateFrom(dateStr)
                                                    recurringStartManuallyEdited = false
                                                }
                                            },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = RecurringFrequency.values().size
                                            ),
                                            colors = SegmentedButtonDefaults.colors(
                                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Text(stringResource(labelRes))
                                        }
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.register_recurring_start_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = recurringStartDate.toDisplayDate(),
                                        onValueChange = { },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                showRecurringStartPicker = true
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.CalendarToday,
                                                    contentDescription = stringResource(R.string.register_pick_date)
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
                                            .clickable { showRecurringStartPicker = true }
                                    )
                                }

                                Text(
                                    text = stringResource(R.string.register_recurring_end_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = if (recurringEndDate.isBlank()) "" else recurringEndDate.toDisplayDate(),
                                        onValueChange = { },
                                        readOnly = true,
                                        placeholder = { Text(stringResource(R.string.register_recurring_end_date_placeholder)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                showRecurringEndPicker = true
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.CalendarToday,
                                                    contentDescription = stringResource(R.string.register_pick_date)
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
                                            .clickable { showRecurringEndPicker = true }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val type = selectedType ?: return@Button
                        val value = amount.parseCurrencyToDouble()
                        if (transactionToEdit != null && transactionToEdit?.transactionId != 0) {
                            viewModel.updateTransaction(
                                transactionId = transactionToEdit?.transactionId ?: 0,
                                date = dateStr,
                                description = description.trim(),
                                type = type,
                                category = selectedCategory,
                                amount = value
                            )
                        } else {
                            viewModel.saveTransaction(
                                date = dateStr,
                                description = description.trim(),
                                type = type,
                                category = selectedCategory,
                                amount = value
                            )
                            if (isRecurring) {
                                viewModel.createRecurringFromTransaction(
                                    description = description.trim(),
                                    type = type,
                                    category = selectedCategory,
                                    amount = value,
                                    originalTransactionDate = dateStr,
                                    startDate = recurringStartDate,
                                    endDate = recurringEndDate.ifBlank { null },
                                    frequency = recurringFrequency
                                )
                            }
                        }
                        viewModel.clearTransactionToEdit()
                        onClick.invoke()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = isFormValid
                ) {
                    Text(stringResource(R.string.register_save))
                }

                if (transactionToEdit != null && transactionToEdit?.transactionId != 0) {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTransaction(transactionToEdit!!.transactionId)
                            viewModel.clearTransactionToEdit()
                            onClick.invoke()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Text(stringResource(R.string.register_delete))
                    }
                }
            }
        }
    )
}
