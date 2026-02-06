package com.renderson.cashflowapp.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.Transaction
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme

/**
 * Gráfico de linhas: entradas (verde) e saídas (vermelho).
 * O eixo X mostra apenas os dias que têm transações (dados).
 */
@Composable
fun SaldoChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    colorEntradas: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    colorSaidas: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error
) {
    val (entradasPorData, saidasPorData) = aggregateByDate(transactions)
    val datasComDados = (entradasPorData.keys + saidasPorData.keys).toSortedSet().toList()
    if (datasComDados.isEmpty()) return

    val entradasEntries = datasComDados.mapIndexed { index, date ->
        Entry(index.toFloat(), entradasPorData.getOrDefault(date, 0f))
    }
    val saidasEntries = datasComDados.mapIndexed { index, date ->
        Entry(index.toFloat(), saidasPorData.getOrDefault(date, 0f))
    }

    val maxY = (entradasPorData.values.maxOrNull() ?: 0f)
        .coerceAtLeast(saidasPorData.values.maxOrNull() ?: 0f)
        .coerceAtLeast(1f)

    val xLabels = datasComDados.map { it.takeLast(2) }
    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                isScaleXEnabled = false
                isScaleYEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val dsEntradas = LineDataSet(entradasEntries, "").apply {
                color = colorEntradas.toArgb()
                setCircleColor(colorEntradas.toArgb())
                lineWidth = 2.5f
                setDrawCircles(true)
                setDrawValues(true)
                setDrawFilled(true)
                fillColor = colorEntradas.copy(alpha = 0.2f).toArgb()
                valueFormatter = CurrencyValueFormatter()
                mode = LineDataSet.Mode.LINEAR
                enableDashedLine(12f, 8f, 0f)
            }
            val dsSaidas = LineDataSet(saidasEntries, "").apply {
                color = colorSaidas.toArgb()
                setCircleColor(colorSaidas.toArgb())
                lineWidth = 2.5f
                setDrawCircles(true)
                setDrawValues(true)
                setDrawFilled(true)
                fillColor = colorSaidas.copy(alpha = 0.2f).toArgb()
                valueFormatter = CurrencyValueFormatter()
                mode = LineDataSet.Mode.LINEAR
                enableDashedLine(12f, 8f, 0f)
            }

            chart.data = LineData(dsEntradas, dsSaidas)

            chart.axisLeft.apply {
                textColor = textColorArgb
                axisMinimum = 0f
                axisMaximum = maxY * 1.1f
                setDrawGridLines(true)
                setDrawAxisLine(true)
            }

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = textColorArgb
                valueFormatter = IndexAxisValueFormatter(xLabels)
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
            }

            chart.invalidate()
        }
    )
}

/** Soma entradas e saídas por data (yyyy-MM-dd). */
private fun aggregateByDate(transactions: List<Transaction>): Pair<Map<String, Float>, Map<String, Float>> {
    val entradas = mutableMapOf<String, Float>()
    val saidas = mutableMapOf<String, Float>()
    for (t in transactions) {
        when (t.type) {
            TypeExtract.DEPOSIT -> entradas[t.date] = (entradas[t.date] ?: 0f) + t.amount.toFloat()
            TypeExtract.PAYMENT -> saidas[t.date] = (saidas[t.date] ?: 0f) + t.amount.toFloat()
            else -> { }
        }
    }
    return Pair(entradas, saidas)
}

private class CurrencyValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value == 0f) "" else "R$${value.toInt()}"
    }
}
