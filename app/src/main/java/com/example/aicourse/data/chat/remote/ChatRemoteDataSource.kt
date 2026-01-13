package com.example.aicourse.data.chat.remote

import com.example.aicourse.data.chat.remote.model.ChatResponseData
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.prompt.ModelType

/**
 * Интерфейс для удаленного источника данных чата
 * Абстракция над конкретной реализацией API (GigaChat, OpenAI, и т.д.)
 */
interface ChatRemoteDataSource {

    /**
     * Отправляет сообщение в API и получает ответ
     * @param message текст сообщения от пользователя
     * @param config конфигурация запроса (temperature, topP, system message)
     * @param messageHistory история предыдущих сообщений для контекста
     * @return ответ от API с метаданными о токенах
     * @throws Exception если произошла ошибка сети или API
     */
    suspend fun sendMessage(
        config: ChatConfig,
        messageHistory: List<Message> = emptyList()
    ): ChatResponseData

    /**
     * Резолвит тип модели в конкретный идентификатор модели для данного провайдера
     * @param modelType тип модели (FAST, BALANCED, POWERFUL)
     * @return конкретный идентификатор модели провайдера
     */
    fun resolveModel(modelType: ModelType): String
}

/**
 * Конфигурация для запроса к chat API
 * @param temperature креативность модели (0.0 - 1.0)
 * @param topP вероятностный отсечение (0.0 - 1.0)
 * @param maxTokens максимальное количество токенов в ответе
 * @param systemContent системное сообщение для настройки поведения модели (null = не используется)
 * @param model конкретный идентификатор модели провайдера (null = используется модель по умолчанию)
 */
data class ChatConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.1f,
    val maxTokens: Int = 1024,
    val systemContent: String? = null,
    val model: String? = null
)
