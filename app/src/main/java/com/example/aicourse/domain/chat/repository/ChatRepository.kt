package com.example.aicourse.domain.chat.repository

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.chat.promt.SystemPrompt

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

/**
 * Интерфейс репозитория для работы с чатом
 * Определяет контракт взаимодействия с источниками данных (Clean Architecture)
 */
interface ChatRepository {

    /**
     * Отправляет сообщение боту и получает типизированный ответ с метаданными
     * @param message текст сообщения от пользователя
     * @param systemPrompt промпт, определяющий поведение модели и тип ответа
     * @param messageHistory история предыдущих сообщений для контекста
     * @return результат с типизированным ответом и метаданными (токены, имя модели)
     * @throws Exception если произошла ошибка при отправке/получении
     */
    suspend fun sendMessage(
        message: String,
        systemPrompt: SystemPrompt<*>,
        messageHistory: List<Message> = emptyList()
    ): Result<SendMessageResult>

    /**
     * Получает историю сообщений из локального хранилища (если есть)
     * @return список сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>>

    /**
     * Сохраняет сообщение в локальное хранилище
     */
    suspend fun saveMessage(message: String, isUser: Boolean): Result<Unit>

    /**
     * Очищает историю сообщений
     */
    suspend fun clearHistory(): Result<Unit>
}
