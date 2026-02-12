package com.renderson.cashflowapp.classifier

import com.renderson.cashflowapp.enums.TransactionCategory

class TransactionCategoryClassifier {

    private val keywordToCategory: Map<List<String>, TransactionCategory> = mapOf(
        listOf("padaria", "mercado", "supermercado", "restaurante", "lanchonete", "açaí", "ifood", "rappi", "uber eats", "alimentação", "alimento") to TransactionCategory.ALIMENTACAO,
        listOf("posto", "conveniência", "conveniencia", "extra") to TransactionCategory.CONVENIENCIA,
        listOf("uber", "99", "taxi", "ônibus", "metro", "combustível", "gasolina", "estacionamento", "pedágio", "transporte", "posto") to TransactionCategory.TRANSPORTE,
        listOf("aluguel", "condomínio", "luz", "água", "energia", "gás", "internet", "telefone", "net", "vivo", "claro", "oi", "moradia", "conta de luz") to TransactionCategory.MORADIA,
        listOf("saúde", "saude", "médico", "medico", "hospital", "clínica", "clinica", "consulta", "remédio", "remedio", "medicamento", "plano de saúde", "convênio", "convenio", "dentista", "exame", "laboratório", "laboratorio", "farmácia", "farmacia", "drogaria", "drogasil", "droga raia") to TransactionCategory.SAUDE,
        listOf("educação", "educacao", "escola", "curso", "faculdade", "universidade", "material escolar", "livro", "mensalidade escolar", "idioma", "aula") to TransactionCategory.EDUCACAO,
        listOf("cinema", "streaming", "netflix", "spotify", "lazer", "academia", "jogo", "game") to TransactionCategory.LAZER,
        listOf("depósito", "deposito", "transferência", "transferencia", "ted", "pix recebido", "salário", "salario") to TransactionCategory.DEPOSITO,
        listOf("cartão", "cartao", "crédito", "credito", "fatura", "cartão de credito", "cartão de crédito") to TransactionCategory.CARTAO_CREDITO,
        listOf("poupança", "poupanca", "cofrinho", "investimento", "investir", "aplicação", "aplicacao", "guardar", "reserva", "tesouro", "cdb", "lci", "lca", "fundos", "ações", "acoes", "renda fixa", "renda variável", "renda variavel") to TransactionCategory.POUPANCA_COFRINHO
    )

    private val allKeywords: List<Pair<String, TransactionCategory>> = keywordToCategory
        .flatMap { (keywords, category) -> keywords.map { it to category } }

    fun classify(description: String): TransactionCategory {
        val normalized = description.lowercase().trim()
        if (normalized.isBlank()) return TransactionCategory.OUTROS
        return allKeywords
            .firstOrNull { (keyword, _) -> normalized.contains(keyword) }
            ?.second
            ?: TransactionCategory.OUTROS
    }
}
