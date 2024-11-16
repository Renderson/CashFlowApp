package com.renderson.cashflowapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.YearEntity

data class YearWithMonths(
    @Embedded var year: YearEntity = YearEntity(),
    @Relation(
        parentColumn = "yearId",
        entityColumn = "yearId",
        entity = MonthEntity::class
    )
    var months: List<MonthWithTransactions> = emptyList()
)