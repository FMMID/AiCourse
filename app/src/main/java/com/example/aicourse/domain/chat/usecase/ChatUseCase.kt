package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.ComplexBotMessage
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.promt.SystemPrompt
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
        val dataForSend = chatStrategy.prepareData(
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
                    toolResult = null
                )
            )

            is DataForSend.RemoteCall -> {
                val sendMessageResult = chatRepository.sendMessage(
                    message = dataForSend.sendToChatDataModel.message,
                    systemPrompt = dataForSend.sendToChatDataModel.systemPrompt,
                    messageHistory = dataForSend.sendToChatDataModel.messageHistory
                )

                sendMessageResult.map { result ->
                    val dataForReceive = chatStrategy.processReceivedData(
                        sendMessageResult = result,
                        sendToChatDataModel = dataForSend.sendToChatDataModel
                    )
                    when (dataForReceive) {
                        is DataForReceive.Simple -> {
                            ComplexBotMessage(
                                message = dataForReceive.message,
                                activePrompt = dataForReceive.activePrompt,
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
        return chatRepository.clearHistory()
    }

    /**
     * Получает историю сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>> {
        return chatRepository.getMessageHistory()
    }
}
