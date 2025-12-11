package com.example.aicourse.data.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

class ContextRepositoryImp(
    private val summarizeContextDataSource: SummarizeContextDataSource
) : ContextRepository {

    override suspend fun summarizeContext(messageHistory: List<Message>): ContextSummaryInfo {
        TODO("call summarizeContextDataSource")
    }
}