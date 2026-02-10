package com.renderson.cashflowapp.model

/**
 * Estrutura do arquivo de backup JSON.
 * Usado para exportar e restaurar transações.
 */
data class BackupData(
    val version: Int = 1,
    val exportedAt: String,
    val transactions: List<BackupTransaction>
)

/**
 * Transação no formato de backup (sem IDs).
 * Na restauração, addTransaction gera novos IDs.
 */
data class BackupTransaction(
    val date: String,
    val description: String,
    val type: String,
    val category: String,
    val amount: Double
)
