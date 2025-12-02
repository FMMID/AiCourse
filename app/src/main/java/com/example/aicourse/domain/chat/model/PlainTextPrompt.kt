package com.example.aicourse.domain.chat.model

/**
 * Дефолтный промпт для обычного текстового общения
 * Не использует system role (content = null)
 * Это стандартное поведение чата без специальных инструкций
 * Используется как fallback, когда другие промпты не срабатывают
 */
data class PlainTextPrompt(
    override val temperature: Float = 0.7f,
    override val topP: Float = 0.1f,
    override val content: String? = null
) : SystemPrompt<PlainTextResponse> {

    /**
     * PlainTextPrompt - fallback промпт, не имеет триггеров
     * @return всегда false
     */
    override fun matches(message: String): Boolean = false

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
