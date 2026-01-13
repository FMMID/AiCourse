package com.example.aicourse.prompt.dynamicSystemPrompt

import com.example.aicourse.prompt.BotResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ от DynamicSystemPrompt
 * Используется для обычных текстовых ответов в режиме динамического промпта
 *
 * @param rawContent исходный текст ответа от API
 */
@Serializable
@SerialName("dynamic_system_prompt")
data class DynamicSystemPromptResponse(
    override val rawContent: String
) : BotResponse
