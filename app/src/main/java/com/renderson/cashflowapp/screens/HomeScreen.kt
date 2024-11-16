package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.model.CardItems
import com.renderson.cashflowapp.util.components.CashFlowAppBar
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    name: String,
    viewModel: CashFlowViewModel,
    onClick: () -> Unit
) {

    val itemsCards = listOf(
        CardItems(
            title = "Total \nEntradas",
            type = TypeExtract.DEPOSIT,
            total = viewModel.deposit.collectAsState().value,
            icon = Icons.AutoMirrored.Outlined.CallReceived
        ),
        CardItems(
            title = "Total \nSaídas",
            type = TypeExtract.PAYMENT,
            total = viewModel.payment.collectAsState().value,
            icon = Icons.AutoMirrored.Outlined.CallMade
        ),
        CardItems(
            title = "Investimentos\n(Em breve)",
            type = TypeExtract.INVESTMENT,
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
                iconBackVisible = false
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                CurrentBalanceComposable(viewModel)
                LazyRow(
                    modifier = Modifier
                        .padding(all = 8.dp)
                ) {
                    items(itemsCards) { itemsCards ->
                        ItemCardList(itemsCards)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 26.dp),
                        text = "Atividades recentes",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyColumn(
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        val transactions = viewModel.extract.value?.years?.last()?.months?.last()?.transactions.orEmpty()
                        items(transactions) { transaction ->
                            ItemExtractList(transaction)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CurrentBalanceComposable(viewModel: CashFlowViewModel) {
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
                text = viewModel.getBalance().formatForBrazilianCurrency(),
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
                text = "Seu valor em caixa.",
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

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        viewModel = viewModel(),
        name = "Home"
    ) {}
}