package com.example.aicourse.domain.chat.promt.plain

import com.example.aicourse.domain.chat.promt.BotResponse

/**
 * Ответ для обычного текстового общения
 * Просто содержит сырой текст ответа без дополнительной обработки
 */
data class PlainTextResponse(
    override val rawContent: String
) : BotResponse
