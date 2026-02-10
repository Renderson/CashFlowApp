package com.renderson.cashflowapp.usecase

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.renderson.cashflowapp.model.BackupData
import com.renderson.cashflowapp.model.BackupTransaction
import com.renderson.cashflowapp.model.Transaction
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exporta transações para formato JSON de backup.
 * Escreve diretamente no OutputStream fornecido.
 */
class ExportBackupUseCase @javax.inject.Inject constructor() {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    /**
     * Gera o JSON de backup e escreve no stream.
     * @param transactions Lista de transações a exportar
     * @param outputStream Stream de destino (ex: contentResolver.openOutputStream(uri))
     * @return Result.success(Unit) ou Result.failure(Exception)
     */
    operator fun invoke(transactions: List<Transaction>, outputStream: OutputStream): Result<Unit> {
        return try {
            val backupTransactions = transactions
                .sortedBy { it.date }
                .map { t ->
                    BackupTransaction(
                        date = t.date,
                        description = t.description,
                        type = t.type.name,
                        category = t.category.name,
                        amount = t.amount
                    )
                }
            val backupData = BackupData(
                version = 1,
                exportedAt = dateFormat.format(Date()),
                transactions = backupTransactions
            )
            val json = gson.toJson(backupData)
            outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
