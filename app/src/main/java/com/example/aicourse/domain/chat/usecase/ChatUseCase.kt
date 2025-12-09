package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.BotResponse
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SystemPrompt
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.model.dynamic.DynamicSystemPrompt
import com.example.aicourse.domain.chat.model.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.model.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.model.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.model.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository

/**
 * Use Case для работы с чатом
 * Содержит бизнес-логику отправки сообщений
 * Зависит только от интерфейса ChatRepository (Clean Architecture)
 */
class ChatUseCase(
    private val chatRepository: ChatRepository
) {

    /**
     * Отправляет сообщение боту через репозиторий
     * Определяет промпт на основе триггеров или использует текущий активный
     * @param message текст сообщения от пользователя
     * @param currentPrompt текущий активный промпт
     * @param messageHistory история предыдущих сообщений для контекста
     * @return Result с типизированным ответом и новым промптом
     */
    suspend fun sendMessageToBot(
        message: String,
        currentPrompt: SystemPrompt<*>,
        messageHistory: List<Message> = emptyList()
    ): Result<ChatResponse> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        if (isResetCommand(message)) {
            return Result.failure(IllegalArgumentException("Используйте ResetPrompt intent для сброса"))
        }

        val newPrompt = extractSystemPromptFromContent(message, currentPrompt) ?: currentPrompt
        val localResponse = handleMessage(newPrompt, message)
        if (localResponse != null) return Result.success(localResponse)

        val cleanedMessage = prepareMessageForSending(newPrompt, message)
        val historyToSend = prepareHistoryForSending(newPrompt, messageHistory)

        val result = chatRepository.sendMessage(cleanedMessage, newPrompt, historyToSend)
        return result.map { sendMessageResult ->
            ChatResponse(
                botResponse = sendMessageResult.botResponse,
                newPrompt = newPrompt,
                tokenUsage = sendMessageResult.tokenUsage,
                modelName = sendMessageResult.modelName
            )
        }
    }

    /**
     * Очищает историю чата
     */
    suspend fun clearChatHistory(): Result<Unit> {
        return chatRepository.clearHistory()
    }

    /**
     * Получает историю сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>> {
        return chatRepository.getMessageHistory()
    }

    /**
     * Проверяет команды сброса промпта
     */
    private fun isResetCommand(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()
        return lowerMessage == "/reset" || lowerMessage == "/plain"
    }

    /**
     * Извлекает подходящий SystemPrompt на основе триггеров в сообщении
     * Проходит по списку доступных промптов и возвращает первый подошедший
     *
     * @param content текст сообщения от пользователя
     * @param currentPrompt активный промпт в рамках текущего чата
     * @return подходящий SystemPrompt или null если триггеров не найдено
     */
    private fun extractSystemPromptFromContent(
        content: String,
        currentPrompt: SystemPrompt<*>,
    ): SystemPrompt<*>? {
        val availablePrompts = listOf(
            JsonOutputPrompt(),
            BuildComputerAssistantPrompt(),
            DynamicSystemPrompt(currentPrompt),
            DynamicTemperaturePrompt(currentPrompt),
            DynamicModelPrompt(currentPrompt),
        )

        return availablePrompts.firstOrNull { prompt ->
            prompt.matches(content)
        }
    }

    private fun handleMessage(activePrompt: SystemPrompt<*>, message: String): ChatResponse? {
        val localResponse = activePrompt.handleMessageLocally(message)
        return localResponse?.let {
            ChatResponse(
                botResponse = it,
                newPrompt = activePrompt
            )
        }
    }

    /**
     * Формирует сообщение для отправки к API на основе активного промпта
     * Некоторые промпты могут изменять сообщение перед отправкой
     *
     * @param prompt активный системный промпт
     * @param message исходное сообщение пользователя
     * @return обработанное сообщение для отправки
     */
    private fun prepareMessageForSending(prompt: SystemPrompt<*>, message: String): String {
        return when (prompt) {
            is DynamicTemperaturePrompt -> {
               prompt.extractAndCleanMessage(message)
            }
            is DynamicModelPrompt -> {
               prompt.extractAndCleanMessage(message)
            }
            else -> message
        }
    }

    /**
     * Формирует историю сообщений для отправки к API на основе активного промпта
     * Некоторые промпты могут не использовать историю
     *
     * @param prompt активный системный промпт
     * @param messageHistory полная история сообщений
     * @return история для отправки (может быть пустой для некоторых промптов)
     */
    private fun prepareHistoryForSending(
        prompt: SystemPrompt<*>,
        messageHistory: List<Message>
    ): List<Message> {
        return when (prompt) {
            is DynamicTemperaturePrompt, is DynamicModelPrompt-> emptyList()
            else -> messageHistory
        }
    }
}

/**
 * Результат отправки сообщения с типизированным ответом и новым промптом
 * @property botResponse типизированный ответ бота
 * @property newPrompt активный промпт после обработки сообщения
 * @property tokenUsage статистика использования токенов (null если не предоставлено)
 * @property modelName имя использованной модели (null если не установлено)
 */
data class ChatResponse(
    val botResponse: BotResponse,
    val newPrompt: SystemPrompt<*>,
    val tokenUsage: TokenUsage? = null,
    val modelName: String? = null
)
