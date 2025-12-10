package com.example.aicourse.domain.chat.promt.dynamicSystemPrompt

import com.example.aicourse.domain.chat.promt.BotResponse

/**
 * Ответ от DynamicSystemPrompt
 * Используется для обычных текстовых ответов в режиме динамического промпта
 *
 * @param rawContent исходный текст ответа от API
 */
data class DynamicSystemPromptResponse(
    override val rawContent: String
) : BotResponse
