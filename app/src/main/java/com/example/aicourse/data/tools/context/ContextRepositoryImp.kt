package com.example.aicourse.data.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.context.ContextRepository

class ContextRepositoryImp(
    private val summarizeContextDataSource: SummarizeContextDataSource
) : ContextRepository {

    override suspend fun summarizeContext(messageHistory: List<Message>): String {
        TODO("call summarizeContextDataSource")
    }
}