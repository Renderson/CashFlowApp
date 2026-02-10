package com.renderson.cashflowapp.extensions

import com.renderson.cashflowapp.enums.SearchPeriod
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.model.Transaction

/**
 * Aplica o filtro de texto + período às transações.
 *
 * - query: texto para buscar em descrição, categoria, tipo e data.
 * - period: intervalo relativo (Tudo, 30, 15, 7 dias).
 * - categoryLabels: mapa de categoria para nome localizado (para busca).
 */
fun List<Transaction>.filterByQueryAndPeriod(
    query: String,
    period: SearchPeriod,
    categoryLabels: Map<TransactionCategory, String>
): List<Transaction> {
    val normalizedQuery = query.trim().lowercase()
    val nowMillis = System.currentTimeMillis()
    val cutoffMillis: Long? = when (period) {
        SearchPeriod.ALL -> null
        SearchPeriod.DAYS_30 -> nowMillis - 30L * 24L * 60L * 60L * 1000L
        SearchPeriod.DAYS_15 -> nowMillis - 15L * 24L * 60L * 60L * 1000L
        SearchPeriod.DAYS_7 -> nowMillis - 7L * 24L * 60L * 60L * 1000L
    }

    return this
        .filter { t ->
            normalizedQuery.isEmpty() ||
                    t.description.lowercase().contains(normalizedQuery) ||
                    (categoryLabels[t.category] ?: "").lowercase().contains(normalizedQuery) ||
                    t.type.label().lowercase().contains(normalizedQuery) ||
                    t.date.toDisplayDate().lowercase().contains(normalizedQuery)
        }
        .filter { t ->
            cutoffMillis == null || dateStringToMillis(t.date) >= cutoffMillis
        }
        .sortedByDescending { it.date }
}

