package com.example.aicourse.domain.chat.model.dynamic

import com.example.aicourse.domain.chat.model.BotResponse

/**
 * Ответ от DynamicSystemPrompt
 * Используется для обычных текстовых ответов в режиме динамического промпта
 *
 * @param rawContent исходный текст ответа от API
 */
data class DynamicSystemPromptResponse(
    override val rawContent: String
) : BotResponse
