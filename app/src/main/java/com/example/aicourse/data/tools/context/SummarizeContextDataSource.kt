package com.example.aicourse.data.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

interface SummarizeContextDataSource {

    suspend fun summarizeContext(messageHistory: List<Message>, existContextSummary: ContextSummaryInfo?): ContextSummaryInfo
}