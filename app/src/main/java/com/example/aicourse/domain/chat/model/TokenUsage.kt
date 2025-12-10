package com.example.aicourse.domain.chat.model

/**
 * Статистика использования токенов для отображения в UI
 *
 * @property promptTokens количество токенов в запросе (null если не предоставлено)
 * @property completionTokens количество токенов в ответе (null если не предоставлено)
 * @property totalTokens общее количество токенов (null если не предоставлено)
 */
data class TokenUsage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null,
    val maxAvailableTokens: Int? = null
) {
    /**
     * Проверяет, содержит ли объект хотя бы одно значение
     */
    fun hasData(): Boolean = promptTokens != null || completionTokens != null || totalTokens != null
}
