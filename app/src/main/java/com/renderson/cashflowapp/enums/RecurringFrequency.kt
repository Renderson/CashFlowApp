package com.renderson.cashflowapp.enums

import java.time.LocalDate

enum class RecurringFrequency {
    WEEKLY,
    MONTHLY,
    YEARLY;

    fun nextDate(fromDate: LocalDate): LocalDate = when (this) {
        WEEKLY -> fromDate.plusWeeks(1)
        MONTHLY -> fromDate.plusMonths(1)
        YEARLY -> fromDate.plusYears(1)
    }

    companion object {
        fun fromString(value: String?): RecurringFrequency =
            value?.let {
                runCatching { valueOf(it) }.getOrNull()
            } ?: MONTHLY
    }
}
