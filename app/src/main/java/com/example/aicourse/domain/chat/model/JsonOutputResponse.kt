package com.example.aicourse.domain.chat.model

/**
 * Ответ в формате JSON со структурированными данными
 * Формат ответа: { "title": "...", "body": "..." } или { "error": "..." }
 */
data class JsonOutputResponse(
    override val rawContent: String,
    val isValid: Boolean,
    val title: String? = null,
    val body: String? = null,
    val error: String? = null
) : BotResponse {

    /**
     * Проверяет, является ли ответ успешным (содержит title и body)
     */
    fun isSuccess(): Boolean = isValid && title != null && body != null

    /**
     * Проверяет, является ли ответ ошибкой (содержит error)
     */
    fun isError(): Boolean = isValid && error != null
}
