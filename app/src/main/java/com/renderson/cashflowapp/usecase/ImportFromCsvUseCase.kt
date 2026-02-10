package com.renderson.cashflowapp.usecase

import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.extensions.parseCurrencyToDouble
import com.renderson.cashflowapp.extensions.parseDisplayDateToStorage
import java.io.InputStream

data class ImportResult(
    val importedCount: Int,
    val skippedCount: Int,
    val errors: List<String>
)

/**
 * Importa transações a partir de um arquivo CSV no formato do export.
 * Estratégia: adicionar (append) às transações existentes.
 */
class ImportFromCsvUseCase @javax.inject.Inject constructor(
    private val repository: ClashFlowRepository
) {

    suspend operator fun invoke(inputStream: InputStream): Result<ImportResult> {
        return try {
            val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()
            if (lines.isEmpty()) {
                return Result.success(ImportResult(0, 0, emptyList()))
            }
            val content = lines.joinToString("\n").trimStart('\uFEFF')
            val allLines = content.lines()
            val dataLines = if (allLines.size > 1) allLines.drop(1) else emptyList()

            var importedCount = 0
            var skippedCount = 0
            val errors = mutableListOf<String>()

            dataLines.forEachIndexed { index, line ->
                val lineNum = index + 2
                val columns = parseCsvLine(line)
                if (columns.size < 5) {
                    skippedCount++
                    errors.add("Linha $lineNum: colunas insuficientes")
                    return@forEachIndexed
                }
                val dateStr = columns[0].trim()
                val description = columns[1].trim()
                val typeStr = columns[2].trim()
                val categoryStr = columns[3].trim()
                val amountStr = columns[4].trim()

                val date = dateStr.parseDisplayDateToStorage()
                if (date == null || date.length < 10) {
                    skippedCount++
                    errors.add("Linha $lineNum: data inválida '$dateStr'")
                    return@forEachIndexed
                }

                val type = CsvImportMappings.mapType(typeStr)
                val category = CsvImportMappings.mapCategory(categoryStr)
                val amount = amountStr.parseCurrencyToDouble()

                runCatching {
                    repository.addTransaction(date, description, type, category, amount)
                    importedCount++
                }.onFailure {
                    skippedCount++
                    errors.add("Linha $lineNum: ${it.message}")
                }
            }

            Result.success(ImportResult(importedCount, skippedCount, errors))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse de linha CSV com separador ";", respeitando campos entre aspas.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    val sb = StringBuilder()
                    i++
                    while (i < line.length) {
                        when (line[i]) {
                            '"' -> {
                                i++
                                if (i < line.length && line[i] == '"') {
                                    sb.append('"')
                                    i++
                                } else {
                                    break
                                }
                            }
                            else -> {
                                sb.append(line[i])
                                i++
                            }
                        }
                    }
                    result.add(sb.toString())
                    if (i < line.length && line[i] == ';') i++
                }
                ch == ';' -> {
                    result.add("")
                    i++
                }
                else -> {
                    val sb = StringBuilder()
                    while (i < line.length && line[i] != ';') {
                        sb.append(line[i])
                        i++
                    }
                    result.add(sb.toString().trim())
                    if (i < line.length) i++
                }
            }
        }
        return result
    }
}
