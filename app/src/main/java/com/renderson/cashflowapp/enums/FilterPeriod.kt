package com.renderson.cashflowapp.enums

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class FilterPeriod {
    CURRENT_MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    ONE_YEAR,
    ALL,
    CUSTOM
}

private val dbDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/**
 * Calcula o intervalo [startDate, endDate] em formato yyyy-MM-dd
 * com base na data atual e no período selecionado.
 */
fun FilterPeriod.dateRangeFromToday(): Pair<String, String> {
    val cal = Calendar.getInstance()
    val todayStr = dbDateFormatter.format(cal.time)

    return when (this) {
        FilterPeriod.CURRENT_MONTH -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = dbDateFormatter.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val end = dbDateFormatter.format(cal.time)
            start to end
        }

        FilterPeriod.THREE_MONTHS -> {
            cal.add(Calendar.MONTH, -3)
            val start = dbDateFormatter.format(cal.time)
            start to todayStr
        }

        FilterPeriod.SIX_MONTHS -> {
            cal.add(Calendar.MONTH, -6)
            val start = dbDateFormatter.format(cal.time)
            start to todayStr
        }

        FilterPeriod.ONE_YEAR -> {
            cal.add(Calendar.MONTH, -12)
            val start = dbDateFormatter.format(cal.time)
            start to todayStr
        }

        FilterPeriod.CUSTOM -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = dbDateFormatter.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val end = dbDateFormatter.format(cal.time)
            start to end
        }

        FilterPeriod.ALL -> throw UnsupportedOperationException("FilterPeriod.ALL does not use a date range; filter in ViewModel with full list.")
    }
}
