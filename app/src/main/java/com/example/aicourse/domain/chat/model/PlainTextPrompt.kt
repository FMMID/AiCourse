package com.example.aicourse.domain.chat.model

/**
 * Дефолтный промпт для обычного текстового общения
 * Не использует system role (content = null)
 * Это стандартное поведение чата без специальных инструкций
 */
data class PlainTextPrompt(
    override val temperature: Float = 0.7f,
    override val topP: Float = 0.1f,
    override val content: String? = null
) : SystemPrompt<PlainTextResponse> {

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
