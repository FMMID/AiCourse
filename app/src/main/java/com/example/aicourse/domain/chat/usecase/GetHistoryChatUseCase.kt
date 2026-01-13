package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.core.BaseUseCase
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.repository.ChatRepository

class GetHistoryChatUseCase(
    private val chatRepository: ChatRepository
) : BaseUseCase<String, ChatStateModel> {

    override suspend fun invoke(input: String): Result<ChatStateModel> {
        return chatRepository.getChatState(chatId = input)
    }
}