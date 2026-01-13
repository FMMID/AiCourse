package com.example.aicourse.prompt.json

import com.example.aicourse.prompt.BotResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ в формате JSON со структурированными данными
 * Формат ответа: { "title": "...", "body": "..." } или { "error": "..." }
 */
@Serializable
@SerialName("json_output")
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
