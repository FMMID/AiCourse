package com.example.aicourse.prompt.dynamicTemperature

import com.example.aicourse.prompt.BotResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ от DynamicTemperaturePrompt
 * Используется для текстовых ответов в режиме динамической температуры
 *
 * @param rawContent исходный текст ответа от API
 */
@Serializable
@SerialName("dynamic_temperature")
data class DynamicTemperatureResponse(
    override val rawContent: String
) : BotResponse
