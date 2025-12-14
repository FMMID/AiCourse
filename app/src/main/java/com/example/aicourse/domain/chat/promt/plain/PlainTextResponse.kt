package com.example.aicourse.domain.chat.promt.plain

import com.example.aicourse.domain.chat.promt.BotResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ для обычного текстового общения
 * Просто содержит сырой текст ответа без дополнительной обработки
 */
@Serializable
@SerialName("plain_text")
data class PlainTextResponse(
    override val rawContent: String
) : BotResponse
