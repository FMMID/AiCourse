package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.core.BaseUseCase
import com.example.aicourse.domain.chat.model.RagMode
import com.example.aicourse.domain.chat.strategy.ChatStrategy

class SetRagModelUseCase(
    private val chatStrategy: ChatStrategy
) : BaseUseCase<RagMode, Unit> {

    override suspend fun invoke(input: RagMode): Result<Unit> {
        chatStrategy.setRagMode(input)
        return Result.success(Unit)
    }
}