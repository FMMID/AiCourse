package com.example.aicourse.domain.chat.promt.dynamicModel

import com.example.aicourse.domain.chat.promt.BotResponse

/**
 * Ответ для DynamicModelPrompt
 * Обертка для обычного текстового ответа
 *
 * @property rawContent сырой текст ответа от модели
 */
data class DynamicModelResponse(
    override val rawContent: String
) : BotResponse