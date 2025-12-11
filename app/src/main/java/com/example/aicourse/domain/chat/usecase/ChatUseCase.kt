package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.chat.strategy.PrepareDataForSendStrategy
import com.example.aicourse.domain.chat.model.ComplexBotMessage
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.repository.SendMessageResult
import com.example.aicourse.domain.chat.util.TokenStatisticsCalculator
import java.util.UUID

/**
 * Use Case для работы с чатом
 * Содержит бизнес-логику отправки сообщений
 * Зависит только от интерфейса ChatRepository (Clean Architecture)
 */
class ChatUseCase(
    private val chatRepository: ChatRepository,
    private val prepareDataForSendStrategy: PrepareDataForSendStrategy
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
    ): Result<ComplexBotMessage> {

        //TODO убрать отсюда эти проверки
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        if (isResetCommand(message)) {
            return Result.failure(IllegalArgumentException("Используйте ResetPrompt intent для сброса"))
        }

        val dataForSend = prepareDataForSendStrategy.prepareData(
            SendToChatDataModel(
                message = message,
                systemPrompt = currentPrompt,
                messageHistory = messageHistory
            )
        )

        return when (dataForSend) {
            is DataForSend.LocalResponse -> Result.success(
                ComplexBotMessage(
                    message = dataForSend.responseMessage,
                    activePrompt = dataForSend.activePrompt,
                    activeModelName = dataForSend.activeModelName
                )
            )

            is DataForSend.RemoteCall -> chatRepository.sendMessage(
                message = dataForSend.sendToChatDataModel.message,
                systemPrompt = dataForSend.sendToChatDataModel.systemPrompt,
                messageHistory = dataForSend.sendToChatDataModel.messageHistory
            ).map { sendMessageResult ->
                ComplexBotMessage(
                    message = sendMessageResult.toMessage(messageHistory),
                    activePrompt = dataForSend.sendToChatDataModel.systemPrompt,
                    activeModelName = sendMessageResult.modelName
                )
            }
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
}
