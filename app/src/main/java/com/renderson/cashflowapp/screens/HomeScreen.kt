package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AccessTimeFilled
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.Extract
import com.renderson.cashflowapp.util.components.CashFlowAppBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    name: String,
    onClick: () -> Unit
) {

    // Mock Items
    val items = listOf(
        Extract(cash = "R$ 1.000", description = "Casa", title = "Casa",type = TypeExtract.DEPOSIT, data = "19/02/1984"),
        Extract(cash = "R$ 2.000", description = "Boleto carro", title = "Boleto carro",type = TypeExtract.PAYMENT, data = "19/02/2000"),
        Extract(cash = "R$ 3.000", description = "Boleto Apartamento", title = "Boleto Apartamento",type = TypeExtract.DEPOSIT, data = "19/02/2010"),
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar =  {
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
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    text = "R$ 1.000,00",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    text = "Seu valor em caixa.",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
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
                        items(items) { item ->
                            ItemExtractList(item)
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen("Home") {}
}