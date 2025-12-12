package com.example.aicourse.domain.tools.context

/**
 * Utility для оценки количества токенов в тексте
 *
 * Использует эвристический метод подсчета на основе количества слов.
 * В среднем одно слово соответствует примерно 1.3 токенам для большинства языковых моделей.
 *
 * Примечание: Это приблизительная оценка. Для точного подсчета следует использовать
 * tokenizer конкретной модели.
 */
object TokenEstimator {

    /**
     * Коэффициент конвертации слов в токены
     * Эмпирически определено, что 1 слово ≈ 1.3 токена
     */
    private const val WORD_TO_TOKEN_RATIO = 1.3

    /**
     * Оценивает количество токенов в тексте
     *
     * @param text текст для анализа
     * @return оценочное количество токенов
     */
    fun estimateTokenCount(text: String): Int {
        val wordCount = text.split(Regex("\\s+")).size
        return (wordCount * WORD_TO_TOKEN_RATIO).toInt()
    }
}
