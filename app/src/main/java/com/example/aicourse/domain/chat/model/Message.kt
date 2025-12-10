package com.example.aicourse.domain.chat.model

/**
 * Модель сообщения в чате
 * @param id уникальный идентификатор
 * @param text текст сообщения (rawContent для bot messages)
 * @param type тип сообщения (USER или BOT)
 * @param timestamp время создания
 * @param typedResponse типизированный ответ для продвинутого отображения (только для BOT)
 * @param tokenUsage статистика использования токенов (только для BOT)
 * @param contextLimit лимит контекста из SystemPrompt (только для BOT)
 * @param tokenUsageDiff разница в использовании токенов с предыдущим сообщением (только для BOT)
 */
data class Message(
    val id: String,
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis(),
    val typedResponse: BotResponse? = null,
    val tokenUsage: TokenUsage? = null,
    val contextLimit: Int? = null,
    val tokenUsageDiff: TokenUsageDiff? = null
)

enum class MessageType {
    USER,
    BOT
}
