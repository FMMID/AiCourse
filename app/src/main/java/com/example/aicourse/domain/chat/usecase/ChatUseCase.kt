package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.ComplexBotMessage
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.promt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.repository.SendMessageResult
import com.example.aicourse.domain.chat.util.TokenStatisticsCalculator
import com.example.aicourse.domain.settings.model.SettingsChatModel
import java.util.UUID

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
        messageHistory: List<Message> = emptyList(),
        settingsChatModel: SettingsChatModel
    ): Result<ComplexBotMessage> {

        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        if (isResetCommand(message)) {
            return Result.failure(IllegalArgumentException("Используйте ResetPrompt intent для сброса"))
        }

        val newPrompt = extractSystemPromptFromContent(message, currentPrompt) ?: currentPrompt
        val localResponse = handleMessage(newPrompt, message)
        if (localResponse != null) {
            return Result.success(
                ComplexBotMessage(
                    message = localResponse,
                    activePrompt = newPrompt,
                    activeModelName = null
                )
            )
        }

        val cleanedMessage = prepareMessageForSending(newPrompt, message)
        val historyToSend = prepareHistoryForSending(newPrompt, messageHistory, settingsChatModel)

        return chatRepository.sendMessage(
            message = cleanedMessage,
            systemPrompt = newPrompt,
            messageHistory = historyToSend
        ).map { sendMessageResult ->
            ComplexBotMessage(
                message = sendMessageResult.toMessage(messageHistory),
                activePrompt = newPrompt,
                activeModelName = sendMessageResult.modelName
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

    private fun handleMessage(activePrompt: SystemPrompt<*>, message: String): Message? {
        val localResponse = activePrompt.handleMessageLocally(message)
        return localResponse?.let {
            Message(
                id = UUID.randomUUID().toString(),
                text = localResponse.rawContent,
                type = MessageType.BOT,
                typedResponse = localResponse,
                tokenUsage = null,
                tokenUsageDiff = null
            )
        }
    }

    private fun SendMessageResult.toMessage(messageHistory: List<Message>): Message {
        val previousBotMessage = messageHistory
            .asReversed()
            .firstOrNull { it.type == MessageType.BOT && it.tokenUsage?.hasData() == true }

        val diff = TokenStatisticsCalculator.calculateDiff(
            tokenUsage,
            previousBotMessage
        )

        return Message(
            id = UUID.randomUUID().toString(),
            text = botResponse.rawContent,
            type = MessageType.BOT,
            typedResponse = botResponse,
            tokenUsage = tokenUsage,
            tokenUsageDiff = diff
        )
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
     * @param settingsChatModel настройка чата
     * @return история для отправки (может быть пустой для некоторых промптов)
     */
    private fun prepareHistoryForSending(
        prompt: SystemPrompt<*>,
        messageHistory: List<Message>,
        settingsChatModel: SettingsChatModel
    ): List<Message> {
        return if (settingsChatModel.isUseMessageHistory) {
            messageHistory
        } else {
            emptyList()
        }
    }
}
