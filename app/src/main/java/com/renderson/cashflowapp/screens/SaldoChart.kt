package com.renderson.cashflowapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.extensions.formatForLocalCurrency
import com.renderson.cashflowapp.extensions.label
import com.renderson.cashflowapp.model.MonthlyTrendPoint
import com.renderson.cashflowapp.model.Transaction
import kotlin.math.max
import kotlin.math.min

/**
 * Gráfico de barras (torres): total de saídas (PAYMENT) agrupado por categoria.
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

    var selectedSlice by remember { mutableStateOf<CategorySliceDetail?>(null) }

    // Ao mudar o período/filtro (transactions mudou), deseleciona fatia e esconde o texto de detalhe
    LaunchedEffect(transactions) {
        selectedSlice = null
    }

    val barEntries = totalsByCategory.mapIndexed { index, (_, total) ->
        BarEntry(index.toFloat(), total.toFloat())
    }

    val categoryLabels = totalsByCategory.map { (category, _) -> category.label() }

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
                BarChart(ctx).apply {
                    setTouchEnabled(true)
                    description.isEnabled = false
                    legend.isEnabled = false
                    axisRight.isEnabled = false
                    setDrawGridBackground(false)
                    setScaleEnabled(false)
                    setFitBars(true)
                }
            },
            update = { chart ->
                val barColors = totalsByCategory.mapIndexed { index, _ ->
                    colorPalette[index % colorPalette.size]
                }
                val dataSet = BarDataSet(barEntries, "").apply {
                    colors = barColors
                    setDrawValues(false)
                }
                chart.data = BarData(dataSet).apply {
                    barWidth = 0.6f
                }

                chart.axisLeft.apply {
                    textColor = textColorArgb
                    axisMinimum = 0f
                    setDrawGridLines(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            return value.toDouble().formatForLocalCurrency()
                        }
                    }
                }

                chart.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = textColorArgb
                    granularity = 1f
                    setDrawGridLines(false)
                    setLabelRotationAngle(-45f)
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            val index = value.toInt()
                            return categoryLabels.getOrNull(index) ?: ""
                        }
                    }
                }

                if (selectedSlice == null) {
                    chart.highlightValue(null)
                } else {
                    val selectedIndex = totalsByCategory.indexOfFirst { it.first == selectedSlice?.category }
                    if (selectedIndex >= 0) {
                        chart.highlightValue(selectedIndex.toFloat(), 0)
                    }
                }

                chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val barEntry = e as? BarEntry ?: return
                        val index = barEntry.x.toInt()
                        val detail = totalsByCategory.getOrNull(index)?.let { (category, total) ->
                            CategorySliceDetail(category, total)
                        } ?: return
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
                text = stringResource(R.string.chart_category_total, detail.category.label(), detail.total.formatForLocalCurrency()),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DepositsVsPaymentsBarChart(
    depositTotal: Double,
    paymentTotal: Double,
    modifier: Modifier = Modifier
) {
    val labels = listOf(
        stringResource(R.string.home_chart_deposits_label),
        stringResource(R.string.home_chart_payments_label)
    )
    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()
    val barColors = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb()
    )

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                BarChart(ctx).apply {
                    description.isEnabled = false
                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    setDrawGridBackground(false)
                    setScaleEnabled(false)
                }
            },
            update = { chart ->
                val entries = listOf(
                    BarEntry(0f, depositTotal.toFloat()),
                    BarEntry(1f, paymentTotal.toFloat())
                )
                val dataSet = BarDataSet(entries, "").apply {
                    colors = barColors
                    setDrawValues(false)
                }
                chart.data = BarData(dataSet).apply {
                    barWidth = 0.5f
                }
                chart.axisLeft.apply {
                    textColor = textColorArgb
                    axisMinimum = 0f
                    setDrawGridLines(true)
                }
                chart.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = textColorArgb
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            return labels.getOrNull(value.toInt()) ?: ""
                        }
                    }
                }
                chart.invalidate()
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ChartLegend(
            items = listOf(
                LegendItem(color = MaterialTheme.colorScheme.primary, label = labels[0]),
                LegendItem(color = MaterialTheme.colorScheme.tertiary, label = labels[1])
            )
        )
    }
}

@Composable
fun MonthlyComparisonBarChart(
    data: List<MonthlyTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Text(
            text = stringResource(R.string.home_chart_insufficient_data),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            textAlign = TextAlign.Center
        )
        return
    }

    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()
    val labels = data.map { it.label }
    val currentColor = MaterialTheme.colorScheme.primary
    val previousColor = MaterialTheme.colorScheme.tertiary
    val minValue = data.minOfOrNull { min(it.currentValue, it.previousValue) } ?: 0.0
    val maxValue = data.maxOfOrNull { max(it.currentValue, it.previousValue) } ?: 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                BarChart(ctx).apply {
                    description.isEnabled = false
                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    setDrawGridBackground(false)
                    setScaleEnabled(false)
                }
            },
            update = { chart ->
                val currentEntries = data.mapIndexed { index, point ->
                    BarEntry(index.toFloat(), point.currentValue.toFloat())
                }
                val previousEntries = data.mapIndexed { index, point ->
                    BarEntry(index.toFloat(), point.previousValue.toFloat())
                }

                val currentSet = BarDataSet(currentEntries, "").apply {
                    color = currentColor.toArgb()
                    setDrawValues(false)
                }
                val previousSet = BarDataSet(previousEntries, "").apply {
                    color = previousColor.toArgb()
                    setDrawValues(false)
                }

                val groupSpace = 0.12f
                val barSpace = 0.02f
                val barWidth = 0.4f

                val barData = BarData(currentSet, previousSet).apply {
                    this.barWidth = barWidth
                }
                chart.data = barData

                chart.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = textColorArgb
                    granularity = 1f
                    setDrawGridLines(false)
                    axisMinimum = 0f
                    axisMaximum = 0f + barData.getGroupWidth(groupSpace, barSpace) * data.size
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            val index = value.toInt()
                            return labels.getOrNull(index) ?: ""
                        }
                    }
                }

                chart.axisLeft.apply {
                    axisMinimum = min(0f, minValue.toFloat())
                    axisMaximum = max(0f, maxValue.toFloat())
                    textColor = textColorArgb
                    setDrawGridLines(true)
                }

                barData.groupBars(0f, groupSpace, barSpace)
                chart.invalidate()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        ChartLegend(
            items = listOf(
                LegendItem(color = currentColor, label = stringResource(R.string.home_chart_current_month_label)),
                LegendItem(color = previousColor, label = stringResource(R.string.home_chart_previous_month_label))
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        data.lastOrNull()?.let { latest ->
            Text(
                text = stringResource(
                    R.string.home_chart_monthly_latest_detail,
                    latest.label,
                    latest.currentValue.formatForLocalCurrency(),
                    latest.previousLabel,
                    latest.previousValue.formatForLocalCurrency()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class LegendItem(val color: Color, val label: String)

@Composable
private fun ChartLegend(items: List<LegendItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

