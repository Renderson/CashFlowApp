package com.renderson.cashflowapp.usecase

import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract

/**
 * Mapeamentos para importação de CSV.
 * Aceita strings localizadas (PT e EN) e nomes de enum.
 */
object CsvImportMappings {

    private val typeDisplayNameToEnum = mapOf(
        // PT
        "Depósito" to TypeExtract.DEPOSIT,
        "Pagamento" to TypeExtract.PAYMENT,
        "Investimento" to TypeExtract.INVESTMENT,
        // EN
        "Deposit" to TypeExtract.DEPOSIT,
        "Payment" to TypeExtract.PAYMENT,
        "Investment" to TypeExtract.INVESTMENT,
        // Enum names
        "DEPOSIT" to TypeExtract.DEPOSIT,
        "PAYMENT" to TypeExtract.PAYMENT,
        "INVESTMENT" to TypeExtract.INVESTMENT
    )

    private val categoryDisplayNameToEnum = mapOf(
        // PT
        "Alimentação" to TransactionCategory.ALIMENTACAO,
        "Conveniência" to TransactionCategory.CONVENIENCIA,
        "Transporte" to TransactionCategory.TRANSPORTE,
        "Moradia" to TransactionCategory.MORADIA,
        "Saúde" to TransactionCategory.SAUDE,
        "Educação" to TransactionCategory.EDUCACAO,
        "Lazer" to TransactionCategory.LAZER,
        "Depósito" to TransactionCategory.DEPOSITO,
        "Cartão de crédito" to TransactionCategory.CARTAO_CREDITO,
        "Investimento" to TransactionCategory.POUPANCA_COFRINHO,
        "Outros" to TransactionCategory.OUTROS,
        // EN
        "Food" to TransactionCategory.ALIMENTACAO,
        "Convenience" to TransactionCategory.CONVENIENCIA,
        "Transport" to TransactionCategory.TRANSPORTE,
        "Housing" to TransactionCategory.MORADIA,
        "Health" to TransactionCategory.SAUDE,
        "Education" to TransactionCategory.EDUCACAO,
        "Leisure" to TransactionCategory.LAZER,
        "Credit card" to TransactionCategory.CARTAO_CREDITO,
        "Other" to TransactionCategory.OUTROS,
        // Enum names
        "ALIMENTACAO" to TransactionCategory.ALIMENTACAO,
        "CONVENIENCIA" to TransactionCategory.CONVENIENCIA,
        "TRANSPORTE" to TransactionCategory.TRANSPORTE,
        "MORADIA" to TransactionCategory.MORADIA,
        "SAUDE" to TransactionCategory.SAUDE,
        "EDUCACAO" to TransactionCategory.EDUCACAO,
        "LAZER" to TransactionCategory.LAZER,
        "DEPOSITO" to TransactionCategory.DEPOSITO,
        "CARTAO_CREDITO" to TransactionCategory.CARTAO_CREDITO,
        "POUPANCA_COFRINHO" to TransactionCategory.POUPANCA_COFRINHO,
        "OUTROS" to TransactionCategory.OUTROS
    )

    fun mapType(value: String?): TypeExtract =
        typeDisplayNameToEnum[value?.trim()] ?: TypeExtract.PAYMENT

    fun mapCategory(value: String?): TransactionCategory =
        categoryDisplayNameToEnum[value?.trim()] ?: TransactionCategory.OUTROS
}
