package com.example.aicourse.domain.chat.model

import com.example.aicourse.prompt.BotResponse

/**
 * Результат отправки сообщения с метаданными
 * @property botResponse типизированный ответ бота
 * @property tokenUsage статистика использования токенов
 * @property modelName имя использованной модели
 */
data class SendMessageResult(
    val botResponse: BotResponse,
    val tokenUsage: TokenUsage? = null,
    val modelName: String? = null
)