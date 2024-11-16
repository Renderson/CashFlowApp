package com.renderson.cashflowapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.renderson.cashflowapp.model.MonthEntity
import com.renderson.cashflowapp.model.TransactionEntity

data class MonthWithTransactions(
    @Embedded val month: MonthEntity = MonthEntity(),
    @Relation(
        parentColumn = "monthId",
        entityColumn = "monthId"
    )
    val transactions: List<TransactionEntity> = emptyList()
)