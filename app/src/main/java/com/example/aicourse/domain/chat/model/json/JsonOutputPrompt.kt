package com.example.aicourse.domain.chat.model.json

import com.example.aicourse.domain.chat.model.SystemPrompt
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
    override val content: String = """
        Ты эксперт в любой области знаний. Я буду задавать тебе вопросы, свои ответы формируй по стандарту JSON.
        Структура ответа должна быть такая: { "title":"Основная тема ответа", "body":"Основное содержимое ответа" }.
        Если ты не можешь сформулировать ответ, тогда выводи JSON в таком формате: { "error":"Причина по которой не смог сформулировать JSON" }.
        Если пользователь предлагает свой вариант вывода учитывай только его, если не предлагает - учитывай предыдущий вариант.
    """.trimIndent()
) : SystemPrompt<JsonOutputResponse> {

    companion object {
        private val PREFIXES = listOf("JSON:", "/json ", "/JSON ")

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
     * Срабатывает на префиксы (JSON:, /json) или ключевые фразы
     */
    override fun matches(message: String): Boolean {
        if (PREFIXES.any { prefix -> message.startsWith(prefix, ignoreCase = true) }) {
            return true
        }

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
