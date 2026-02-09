package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.dateStringToMillis
import com.renderson.cashflowapp.extensions.getTodayAsString
import com.renderson.cashflowapp.extensions.millisToDateString
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.extensions.parseBrazilianCurrencyToDouble
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.util.components.CashFlowTextField
import com.renderson.cashflowapp.util.components.TypeInputEnum
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    viewModel: CashFlowViewModel,
    name: String,
    onClick: () -> Unit
) {
    val transactionToEdit by viewModel.transactionToEdit.collectAsState(initial = null)
    var amount by remember(transactionToEdit?.transactionId) {
        mutableStateOf(transactionToEdit?.amount?.formatForBrazilianCurrency() ?: "")
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
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToMillis(getTodayAsString()),
        yearRange = IntRange(2020, 2030)
    )

    val isFormValid = amount.trim().isNotEmpty() &&
        description.trim().isNotEmpty() &&
        dateStr.isNotEmpty() &&
        selectedType != null &&
        amount.parseBrazilianCurrencyToDouble() > 0

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateStr = millisToDateString(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            CashFlowAppBar(
                title = name,
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
                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    hint = "Valor",
                    inputType = TypeInputEnum.CURRENCY,
                    keyboardType = KeyboardType.Decimal,
                    maxLength = 16,
                    onInputChange = { amount = it }
                )

                CashFlowTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    hint = "Ex: Supermercado, Salário...",
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    maxLength = 120,
                    onInputChange = { description = it }
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Data",
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
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Escolher data"
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
                            .clickable { showDatePicker = true }
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Tipo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    SegmentedButton(
                        selected = selectedType == TypeExtract.DEPOSIT,
                        onClick = { selectedType = TypeExtract.DEPOSIT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Entrada")
                    }
                    SegmentedButton(
                        selected = selectedType == TypeExtract.PAYMENT,
                        onClick = { selectedType = TypeExtract.PAYMENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Saída")
                    }
                }

                Button(
                    onClick = {
                        val type = selectedType ?: return@Button
                        val value = amount.parseBrazilianCurrencyToDouble()
                        if (transactionToEdit != null && transactionToEdit?.transactionId != 0) {
                            viewModel.updateTransaction(
                                transactionId = transactionToEdit?.transactionId ?: 0,
                                date = dateStr,
                                description = description.trim(),
                                type = type,
                                amount = value
                            )
                        } else {
                            viewModel.saveTransaction(
                                date = dateStr,
                                description = description.trim(),
                                type = type,
                                amount = value
                            )
                        }
                        viewModel.clearTransactionToEdit()
                        onClick.invoke()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = isFormValid
                ) {
                    Text("Salvar")
                }

                if (transactionToEdit != null && transactionToEdit?.transactionId != 0) {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTransaction(transactionToEdit!!.transactionId)
                            viewModel.clearTransactionToEdit()
                            onClick.invoke()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text("Deletar")
                    }
                }
            }
        }
    )
}
