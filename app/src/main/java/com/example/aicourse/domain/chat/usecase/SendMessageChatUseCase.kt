package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.base.BaseUseCase
import com.example.aicourse.domain.chat.model.ComplexBotMessage
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend

/**
 * Отправляет сообщение боту через репозиторий
 * Определяет промпт на основе триггеров или использует текущий активный
 * @param userMessage сообщение пользователя
 * @return Result с типизированным ответом и новым промптом
 */
class SendMessageChatUseCase(
    private val chatRepository: ChatRepository,
    private val chatStrategy: ChatStrategy
) : BaseUseCase<Message, ComplexBotMessage> {

    override suspend fun invoke(input: Message): Result<ComplexBotMessage> {
        val chatResult = when (val dataForSend = chatStrategy.prepareData(userMessage = input)) {
            is DataForSend.LocalResponse -> Result.success(
                ComplexBotMessage(
                    message = dataForSend.responseMessage,
                    systemPrompt = dataForSend.activePrompt,
                    toolResult = null
                )
            )

            is DataForSend.RemoteCall -> {
                val sendMessageResult = chatRepository.sendMessage(
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

        chatRepository.saveChatSate(chatStrategy.chatStateModel)
        return chatResult
    }
}
