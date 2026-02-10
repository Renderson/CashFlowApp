package com.renderson.cashflowapp.usecase

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.BackupData
import java.io.InputStream

/**
 * Restaura transações a partir de um arquivo de backup JSON.
 * Substitui todos os dados atuais.
 */
class RestoreBackupUseCase @javax.inject.Inject constructor(
    private val repository: ClashFlowRepository
) {

    private val gson = Gson()

    /**
     * Lê o JSON do stream, valida e restaura no repositório.
     * @param inputStream Stream do arquivo (ex: contentResolver.openInputStream(uri))
     * @return Result.success(Unit) ou Result.failure(Exception)
     */
    suspend operator fun invoke(inputStream: InputStream): Result<Unit> {
        return try {
            val json = inputStream.bufferedReader(Charsets.UTF_8).readText()
            val backupData = gson.fromJson(json, BackupData::class.java)
                ?: return Result.failure(IllegalArgumentException("Invalid backup file"))
            val transactions = backupData.transactions
            if (transactions.isEmpty()) {
                return Result.failure(IllegalArgumentException("Backup file has no transactions"))
            }
            repository.restoreFromBackup(transactions)
            Result.success(Unit)
        } catch (e: JsonSyntaxException) {
            Result.failure(IllegalArgumentException("Invalid JSON format", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
