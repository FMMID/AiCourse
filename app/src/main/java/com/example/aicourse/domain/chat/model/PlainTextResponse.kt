package com.example.aicourse.domain.chat.model

/**
 * Ответ для обычного текстового общения
 * Просто содержит сырой текст ответа без дополнительной обработки
 */
data class PlainTextResponse(
    override val rawContent: String
) : BotResponse
