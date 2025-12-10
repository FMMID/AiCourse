package com.example.aicourse.domain.chat.promt.dynamicTemperature

import com.example.aicourse.domain.chat.promt.BotResponse

/**
 * Ответ от DynamicTemperaturePrompt
 * Используется для текстовых ответов в режиме динамической температуры
 *
 * @param rawContent исходный текст ответа от API
 */
data class DynamicTemperatureResponse(
    override val rawContent: String
) : BotResponse
