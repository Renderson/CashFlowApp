package com.renderson.cashflowapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.renderson.cashflowapp.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatForBrazilianCurrency
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.model.Transaction

/**
 * Gráfico de pizza: total de saídas (PAYMENT) agrupado por categoria.
 * Fonte: lista de transações passada (ex.: todas do banco).
 */
@Composable
fun SaldoChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    // soma todas as saídas por categoria
    val totalsByCategory: List<Pair<TransactionCategory, Double>> = transactions
        .filter { it.type == TypeExtract.PAYMENT }
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .filter { it.value > 0.0 }
        .toList()
        .sortedByDescending { it.second }

    if (totalsByCategory.isEmpty()) return

    data class CategorySliceDetail(val category: TransactionCategory, val total: Double)

    val detailsByLabel: Map<String, CategorySliceDetail> = totalsByCategory.associate { (category, total) ->
        category.label() to CategorySliceDetail(category, total)
    }

    var selectedSlice by remember { mutableStateOf<CategorySliceDetail?>(null) }

    // Ao mudar o período/filtro (transactions mudou), deseleciona fatia e esconde o texto de detalhe
    LaunchedEffect(transactions) {
        selectedSlice = null
    }

    val entries = totalsByCategory.map { (category, total) ->
        PieEntry(total.toFloat(), category.label())
    }

    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()
    val colorPalette = listOf(
        Color(0xFFA8D5BA),  // verde pastel
        Color(0xFFA8C8E8),  // azul pastel
        Color(0xFFF5D99C),  // amarelo suave
        Color(0xFFF5B8C4),  // rosa pastel
        Color(0xFFC9B8E8),  // roxo pastel
        Color(0xFFA8E6E0),  // ciano pastel
        Color(0xFFF5C4A8),  // coral pastel
        Color(0xFFD0D0D5),  // cinza suave
    ).map { it.toArgb() }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            factory = { ctx ->
                PieChart(ctx).apply {
                    setTouchEnabled(true)
                    description.isEnabled = false
                    setDrawEntryLabels(true)
                    setUsePercentValues(false)
                    isDrawHoleEnabled = true
                    setHoleColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { chart ->
                val dataSet = PieDataSet(entries, "").apply {
                    colors = colorPalette
                    // não desenhar valores numéricos nas fatias
                    setDrawValues(false)
                }
                chart.data = PieData(dataSet)

                chart.legend.isEnabled = false

                // cores de texto respeitando o tema (claro/escuro)
                chart.setEntryLabelColor(textColorArgb)

                // Volta ao estado normal quando não há fatia selecionada (ex.: após trocar filtro)
                if (selectedSlice == null) {
                    chart.highlightValue(null)
                }

                chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val pieEntry = e as? PieEntry ?: return
                        val label = pieEntry.label ?: return
                        val detail = detailsByLabel[label] ?: return
                        selectedSlice = detail
                    }

                    override fun onNothingSelected() {
                        // manter última seleção; não limpar
                    }
                })

                chart.invalidate()
            }
        )

        selectedSlice?.let { detail ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = stringResource(R.string.chart_category_total, detail.category.label(), detail.total.formatForBrazilianCurrency()),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

