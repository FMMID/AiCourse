package com.example.aicourse.domain.chat.promt.dynamicModel

import com.example.aicourse.domain.chat.promt.BotResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ для DynamicModelPrompt
 * Обертка для обычного текстового ответа
 *
 * @property rawContent сырой текст ответа от модели
 */
@Serializable
@SerialName("dynamic_model")
data class DynamicModelResponse(
    override val rawContent: String
) : BotResponse