package com.example.aicourse.domain.chat.model

import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.chat.promt.SystemPrompt

/**
 * Результат отправки сообщения с типизированным ответом и новым промптом
 * @property botResponse типизированный ответ бота
 * @property newPrompt активный промпт после обработки сообщения
 * @property tokenUsage статистика использования токенов (null если не предоставлено)
 * @property modelName имя использованной модели (null если не установлено)
 */
data class ChatResponseModel(
    val botResponse: BotResponse,
    val newPrompt: SystemPrompt<*>,
    val tokenUsage: TokenUsage? = null,
    val modelName: String? = null
)