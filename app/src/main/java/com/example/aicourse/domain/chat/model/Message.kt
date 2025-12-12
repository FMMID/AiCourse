package com.example.aicourse.domain.chat.model

import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.tools.ToolResult

/**
 * Модель сообщения в чате
 * @param id уникальный идентификатор
 * @param text текст сообщения (rawContent для bot messages)
 * @param type тип сообщения (USER или BOT)
 * @param timestamp время создания
 * @param typedResponse типизированный ответ для продвинутого отображения (только для BOT)
 * @param tokenUsage статистика использования токенов (только для BOT)
 * @param toolResult результат работы инструментов (только для BOT)
 */
data class Message(
    val id: String,
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis(),
    val typedResponse: BotResponse? = null,
    val tokenUsage: TokenUsage? = null,
    val toolResult: ToolResult? = null,
)

enum class MessageType {
    USER,
    BOT,
    SYSTEM  // Системные сообщения о событиях сжатия контекста и т.д.
}
