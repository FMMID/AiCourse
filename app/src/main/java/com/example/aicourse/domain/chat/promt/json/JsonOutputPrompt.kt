package com.example.aicourse.domain.chat.promt.json

import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.SystemPrompt
import org.json.JSONException
import org.json.JSONObject

/**
 * Промпт для получения структурированных ответов в формате JSON
 * Активируется по ключевым словам или префиксам
 *
 * Формат ответа:
 * Success: { "title": "Основная тема", "body": "Содержимое" }
 * Error: { "error": "Причина ошибки" }
 */
data class JsonOutputPrompt(
    override val temperature: Float = 0.5f,
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 1024,
    override val contentResourceId: Int? = R.raw.json_output_prompt
) : SystemPrompt<JsonOutputResponse> {

    companion object {
        private val COMMAND_TRIGGERS = listOf("/json")
        private val PREFIX_TRIGGERS = listOf("JSON:")

        private val PATTERNS = listOf(
            Regex("ответь\\s+в\\s+(формате\\s+)?json", RegexOption.IGNORE_CASE),
            Regex("выведи\\s+(ответ\\s+)?в\\s+json", RegexOption.IGNORE_CASE),
            Regex("формат\\s+(ответа|вывода):?\\s+json", RegexOption.IGNORE_CASE),
            Regex("дай\\s+ответ\\s+в\\s+json", RegexOption.IGNORE_CASE),
            Regex("в\\s+формате\\s+json", RegexOption.IGNORE_CASE)
        )
    }

    /**
     * Проверяет триггеры активации промпта
     * Срабатывает на команды (/json), префиксы (JSON:) или ключевые фразы
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        // Проверка команд: точное совпадение или с пробелом после
        if (COMMAND_TRIGGERS.any { trigger ->
                lowerMessage == trigger || lowerMessage.startsWith("$trigger ")
            }) {
            return true
        }

        // Проверка префиксов (JSON:)
        if (PREFIX_TRIGGERS.any { prefix -> message.startsWith(prefix, ignoreCase = true) }) {
            return true
        }

        // Проверка ключевых фраз
        return PATTERNS.any { pattern -> pattern.containsMatchIn(message) }
    }

    /**
     * Парсит JSON ответ в структурированный объект
     * Обрабатывает как успешные ответы (title/body), так и ошибки (error)
     */
    override fun parseResponse(rawResponse: String): JsonOutputResponse {
        return try {
            val jsonString = extractJson(rawResponse)
            val jsonObject = JSONObject(jsonString)

            when {
                jsonObject.has("error") -> {
                    JsonOutputResponse(
                        rawContent = rawResponse,
                        isValid = true,
                        error = jsonObject.getString("error")
                    )
                }

                jsonObject.has("title") && jsonObject.has("body") -> {
                    JsonOutputResponse(
                        rawContent = rawResponse,
                        isValid = true,
                        title = jsonObject.getString("title"),
                        body = jsonObject.getString("body")
                    )
                }

                else -> {
                    JsonOutputResponse(
                        rawContent = rawResponse,
                        isValid = false,
                        error = "JSON не содержит ожидаемые поля (title/body или error)"
                    )
                }
            }
        } catch (e: JSONException) {
            JsonOutputResponse(
                rawContent = rawResponse,
                isValid = false,
                error = "Не удалось распарсить JSON: ${e.message}"
            )
        }
    }

    /**
     * Извлекает JSON из ответа, удаляя markdown обертки если есть
     * Пример: ```json\n{...}\n``` -> {...}
     */
    private fun extractJson(rawResponse: String): String {
        val trimmed = rawResponse.trim()

        val jsonBlockRegex = Regex("```(?:json)?\\s*\\n?([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = jsonBlockRegex.find(trimmed)

        return if (match != null) {
            match.groupValues[1].trim()
        } else {
            trimmed
        }
    }
}
