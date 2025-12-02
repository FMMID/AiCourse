package com.example.aicourse.domain.chat.model.plain

import com.example.aicourse.domain.chat.model.BotResponse

/**
 * Ответ для обычного текстового общения
 * Просто содержит сырой текст ответа без дополнительной обработки
 */
data class PlainTextResponse(
    override val rawContent: String
) : BotResponse
