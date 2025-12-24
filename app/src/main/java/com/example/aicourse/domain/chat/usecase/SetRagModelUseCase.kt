package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.base.BaseUseCase
import com.example.aicourse.domain.chat.strategy.ChatStrategy

class SetRagModelUseCase(
    private val chatStrategy: ChatStrategy
) : BaseUseCase<Boolean, Unit> {

    override suspend fun invoke(input: Boolean): Result<Unit> {
        chatStrategy.setRagMode(input)
        return Result.success(Unit)
    }
}