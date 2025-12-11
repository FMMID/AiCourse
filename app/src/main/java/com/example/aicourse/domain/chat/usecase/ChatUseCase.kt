package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.ComplexBotMessage
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend

/**
 * Use Case для работы с чатом
 * Содержит бизнес-логику отправки сообщений
 * Зависит только от интерфейса ChatRepository (Clean Architecture)
 */
class ChatUseCase(
    private val chatRepository: ChatRepository,
    private val chatStrategy: ChatStrategy
) {

    /**
     * Отправляет сообщение боту через репозиторий
     * Определяет промпт на основе триггеров или использует текущий активный
     * @param userMessage сообщение пользователя
     * @return Result с типизированным ответом и новым промптом
     */
    suspend fun sendMessageToBot(
        userMessage: Message,
    ): Result<ComplexBotMessage> {
        return when (val dataForSend = chatStrategy.prepareData(userMessage)) {
            is DataForSend.LocalResponse -> Result.success(
                ComplexBotMessage(
                    message = dataForSend.responseMessage,
                    systemPrompt = dataForSend.activePrompt,
                    toolResult = null
                )
            )

            is DataForSend.RemoteCall -> {
                val sendMessageResult = chatRepository.sendMessage(
                    message = dataForSend.message,
                    systemPrompt = dataForSend.activePrompt,
                    messageHistory = dataForSend.messageHistory
                )

                sendMessageResult.map { result ->
                    when (val dataForReceive = chatStrategy.processReceivedData(sendMessageResult = result)) {
                        is DataForReceive.Simple -> {
                            ComplexBotMessage(
                                message = dataForReceive.message,
                                systemPrompt = dataForReceive.activePrompt,
                                toolResult = dataForReceive.toolResult
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Очищает историю чата
     */
    suspend fun clearChatHistory(): Result<Unit> {
        chatStrategy.clear()
        return chatRepository.clearHistory()
    }

    /**
     * Получает историю сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>> {
        return chatRepository.getMessageHistory()
    }
}
