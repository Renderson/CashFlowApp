package com.renderson.cashflowapp.usecase

import com.renderson.cashflowapp.enums.TransactionCategory

class SuggestTransactionCategoryUseCase(
    private val classifier: com.renderson.cashflowapp.classifier.TransactionCategoryClassifier
) {
    operator fun invoke(description: String): TransactionCategory {
        if (description.isBlank()) return TransactionCategory.OUTROS
        return classifier.classify(description)
    }
}
