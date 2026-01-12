package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.core.BaseUseCase
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy

class ClearHistoryChatUseCase(
    private val chatRepository: ChatRepository,
    private val chatStrategy: ChatStrategy
) : BaseUseCase<String, Unit> {

    override suspend fun invoke(input: String): Result<Unit> {
        chatStrategy.clear()
        chatRepository.saveChatSate(chatStrategy.chatStateModel)
        return chatRepository.clearHistory()
    }
}
