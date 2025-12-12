package com.example.aicourse.domain.chat.promt.plain

import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.SystemPrompt

/**
 * Дефолтный промпт для обычного текстового общения
 * Не использует system role (contentResourceId = null)
 * Это стандартное поведение чата без специальных инструкций
 * Используется как fallback, когда другие промпты не срабатывают
 */
data class PlainTextPrompt(
    override val temperature: Float = 0.7f,
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 1024,
    override val contentResourceId: Int = R.raw.test_dnd_promt,
    override var contextSummary: String? = null
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
