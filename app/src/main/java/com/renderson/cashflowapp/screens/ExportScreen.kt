package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.FilterPeriod
import com.renderson.cashflowapp.enums.dateRangeFromToday
import com.renderson.cashflowapp.extensions.dateStringToMillis
import com.renderson.cashflowapp.extensions.getFirstDayOfCurrentMonth
import com.renderson.cashflowapp.extensions.getLastDayOfCurrentMonth
import com.renderson.cashflowapp.extensions.millisToDateString
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.usecase.ExportToCsvUseCase
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExportScreen(
    viewModel: CashFlowViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val extract by viewModel.extract.observeAsState()
    val allTransactions = extract?.years?.flatMap { it.months }?.flatMap { it.transactions } ?: emptyList()

    var selectedPeriod by remember { mutableStateOf(FilterPeriod.CURRENT_MONTH) }
    var customStartDateStr by remember { mutableStateOf(getFirstDayOfCurrentMonth()) }
    var customEndDateStr by remember { mutableStateOf(getLastDayOfCurrentMonth()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToMillis(getFirstDayOfCurrentMonth()),
        yearRange = IntRange(2020, 2030)
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToMillis(getLastDayOfCurrentMonth()),
        yearRange = IntRange(2020, 2030)
    )

    val filteredTransactions = remember(allTransactions, selectedPeriod, customStartDateStr, customEndDateStr) {
        when (selectedPeriod) {
            FilterPeriod.ALL -> allTransactions
            else -> {
                val (start, end) = if (selectedPeriod == FilterPeriod.CUSTOM) {
                    customStartDateStr to customEndDateStr
                } else {
                    selectedPeriod.dateRangeFromToday()
                }
                allTransactions.filter { it.date in start..end }
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyyMMdd_HHmm", Locale.US) }
    val fileName = "${context.getString(R.string.export_filename_prefix)}_${dateFormat.format(java.util.Date())}.csv"

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { outputUri ->
            val exportUseCase = ExportToCsvUseCase()
            exportUseCase(context, filteredTransactions).fold(
                onSuccess = { file ->
                    try {
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.export_success))
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.export_error))
                        }
                    }
                },
                onFailure = {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.export_error))
                    }
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CashFlowAppBar(
                title = stringResource(R.string.export_title),
                colorViews = MaterialTheme.colorScheme.onSurface,
                iconBackVisible = true,
                onIconBackClick = onBack,
                showSearchIcon = false,
                showFilterIcon = false
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.export_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                            selected = period == selectedPeriod,
                            onClick = {
                                selectedPeriod = period
                            },
                            label = { Text(text = period.label()) }
                        )
                    }
                }

                AnimatedVisibility(visible = selectedPeriod == FilterPeriod.CUSTOM) {
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
                    }
                }

                Text(
                    text = if (filteredTransactions.isEmpty()) {
                        stringResource(R.string.export_empty)
                    } else {
                        stringResource(R.string.export_preview, filteredTransactions.size)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Button(
                    onClick = { createDocumentLauncher.launch(fileName) },
                    enabled = filteredTransactions.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.export_button))
                }
            }
        }
    )

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
