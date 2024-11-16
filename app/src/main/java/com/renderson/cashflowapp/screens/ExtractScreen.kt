package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTimeFilled
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.model.Years
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExtractScreen(
    name: String,
    viewModel: CashFlowViewModel,
    onClick: () -> Unit
) {
    val active by remember { mutableStateOf(false) }
    val color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background

    Scaffold (
        topBar = {
            CashFlowAppBar(
                title = name,
                colorViews = MaterialTheme.colorScheme.onSurface,
                iconBackVisible = false
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .background(color)
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                BankStatementScreen(
                    viewModel = viewModel
                )
            }
        }
    )
}


@Preview
@Composable
fun PreviewExtractScreen() {
    ExtractScreen(
        viewModel = viewModel(),
        name = "Extrato"
    ) {}
}