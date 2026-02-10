package com.renderson.cashflowapp.usecase

import android.content.Context
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.extensions.formatForLocalCurrency
import com.renderson.cashflowapp.extensions.toDisplayDate
import com.renderson.cashflowapp.model.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gera arquivo CSV com as transações para exportação (compatível com Excel).
 * Usa separador ";" e encoding UTF-8 com BOM.
 */
class ExportToCsvUseCase {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

    /**
     * Gera o arquivo CSV e retorna o File criado.
     * @param context Context para acessar recursos e criar arquivo
     * @param transactions Lista de transações a exportar (ordenadas por data)
     * @return Result.success(File) ou Result.failure(Exception)
     */
    operator fun invoke(context: Context, transactions: List<Transaction>): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "export").apply { mkdirs() }
            val fileName = "${context.getString(R.string.export_filename_prefix)}_${dateFormat.format(Date())}.csv"
            val file = File(exportDir, fileName)

            val separator = ";"
            val headerDate = context.getString(R.string.export_csv_column_date)
            val headerDesc = context.getString(R.string.export_csv_column_description)
            val headerType = context.getString(R.string.export_csv_column_type)
            val headerCategory = context.getString(R.string.export_csv_column_category)
            val headerAmount = context.getString(R.string.export_csv_column_amount)
            val header = listOf(headerDate, headerDesc, headerType, headerCategory, headerAmount).joinToString(separator)

            val lines = mutableListOf<String>()
            lines.add(header)
            transactions.sortedBy { it.date }.forEach { t ->
                val date = t.date.toDisplayDate()
                val desc = escapeCsvField(t.description)
                val type = context.getString(t.type.labelRes)
                val category = context.getString(t.category.displayNameRes)
                val amount = t.amount.formatForLocalCurrency()
                lines.add(listOf(date, desc, type, category, amount).joinToString(separator))
            }

            val csvContent = lines.joinToString("\n")
            val bom = "\uFEFF"
            file.writeText(bom + csvContent, Charsets.UTF_8)

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsvField(value: String): String {
        return if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
