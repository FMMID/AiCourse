package com.example.aicourse.rag.domain

import android.util.Log

/**
 * Генерирует вариации запроса для улучшения recall в RAG
 */
interface QueryExpander {
    /**
     * Создает список вариаций исходного запроса
     * @param query Исходный запрос пользователя
     * @return Список запросов (включая оригинальный)
     */
    suspend fun expandQuery(query: String): List<String>
}

/**
 * Простой расширитель запросов без использования LLM
 * Использует шаблонные переформулировки
 */
class SimpleQueryExpander : QueryExpander {

    override suspend fun expandQuery(query: String): List<String> {
        val variations = mutableListOf<String>()

        // 1. Оригинальный запрос
        variations.add(query)

        // 2. Переформулировки в вопросительную форму
        if (!query.trim().endsWith("?")) {
            variations.add("$query?")
        }

        // 3. Добавляем контекстные префиксы
        val prefixes = listOf(
            "Расскажи про",
            "Что такое",
            "Информация о"
        )

        // Находим ключевые слова (слова длиннее 3 символов, не предлоги)
        val keywords = query.split(" ")
            .filter { it.length > 3 }
            .filter { it.lowercase() !in stopWords }
            .take(3) // Берём только первые 3 ключевых слова

        if (keywords.isNotEmpty()) {
            val keyPhrase = keywords.joinToString(" ")
            prefixes.forEach { prefix ->
                variations.add("$prefix $keyPhrase")
            }
        }

        Log.d("QueryExpander", "Expanded query into ${variations.size} variations")
        variations.forEachIndexed { index, variant ->
            Log.d("QueryExpander", "  [$index]: $variant")
        }

        return variations.take(3) // Максимум 3 вариации
    }

    companion object {
        private val stopWords = setOf(
            "в", "на", "и", "с", "по", "для", "как", "что", "это",
            "из", "у", "о", "об", "к", "от", "за", "под", "над"
        )
    }
}
