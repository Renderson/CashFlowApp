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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AccessTimeFilled
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.Extract
import com.renderson.cashflowapp.util.components.CashFlowAppBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExtractScreen(name: String, onClick: () -> Unit) {
    // Mock Items
    val extract = listOf(
        Extract(cash = "R$ 1.000", description = "Casa", title = "CASA", type = TypeExtract.DEPOSIT, data = "19/02/1984"),
        Extract(cash = "R$ 2.000", description = "Consertos na neve", title = "NEVE", type = TypeExtract.PAYMENT, icon = Icons.Outlined.CallReceived, data = "19/02/2000"),
        Extract(cash = "R$ 3.000", description = "Ajuste Relogio", title = "RELOGIO", type = TypeExtract.DEPOSIT, icon = Icons.Outlined.CallMade, data = "19/02/2010"),
        Extract(cash = "R$ 4.000", description = "Conserto relogio", title = "CONSERTO", type = TypeExtract.PAYMENT, icon = Icons.Outlined.AccessTimeFilled, data = "19/02/2015"),
        Extract(cash = "R$ 5.000", description = "Transporte", title = "TRANSPORTE", type = TypeExtract.DEPOSIT, icon = Icons.Outlined.AccessibilityNew, data = "19/02/2023"),
    )

    val active by remember { mutableStateOf(false) }
    val color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background

    var text by remember { mutableStateOf("") }
    var padding = if (active) 0.dp else 16.dp

    // Get list DB
    var items = remember {
        mutableStateListOf(
            "Escola",
            "Carro",
            "Casa"
        )
    }

    Scaffold (
        topBar = {
            CashFlowAppBar(
                title = name,
                colorViews = MaterialTheme.colorScheme.onSurface,
                iconBackVisible = false
            )
        },
        content = {
            Column(
                modifier = Modifier.background(color)
            ) {
                //SearchBarCompose(text, items, active)
                LazyColumn {
                    itemsIndexed(extract) { index, item ->
                        Row(
                            modifier = Modifier
                                .padding(
                                    top = 16.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(8.dp),
                                imageVector = if (item.type != TypeExtract.DEPOSIT) Icons.Outlined.CallMade else Icons.Outlined.CallReceived,
                                tint = if (item.type != TypeExtract.DEPOSIT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                contentDescription = "icon"
                            )
                            Column(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = item.title.orEmpty(),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = item.description.orEmpty(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Column(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .align(Alignment.CenterVertically),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.cash.orEmpty(),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        if (index < extract.lastIndex) {
                            Divider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = Color.Gray.copy(0.10f)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchBarCompose(
    text: String,
    items: SnapshotStateList<String>,
    active: Boolean
) {
    var text1 = text
    var active1 = active
    SearchBar(
        modifier = Modifier.fillMaxWidth(),//.padding(horizontal = padding),
        query = text1,
        onQueryChange = {
            text1 = it
        },
        onSearch = {
            if (text1.isNotEmpty()) items.add(text1)
            active1 = false
            text1 = ""
        },
        active = active1,
        onActiveChange = {
            active1 = it
        },
        placeholder = {
            Text(text = "Search")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            if (active1) {
                Icon(
                    modifier = Modifier.clickable {
                        if (text1.isNotEmpty()) {
                            text1 = ""
                        } else {
                            active1 = false
                        }
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon"
                )
            }
        }
    ) {
        items.forEach {
            Row(
                modifier = Modifier.padding(all = 14.dp)
            ) {
                Icon(
                    modifier = Modifier.padding(end = 10.dp),
                    imageVector = Icons.Default.History,
                    contentDescription = "History Icon"
                )
                Text(text = it)
            }
        }
    }
}

@Preview
@Composable
fun PreviewExtractScreen() {
    ExtractScreen("Extrato", {})
}