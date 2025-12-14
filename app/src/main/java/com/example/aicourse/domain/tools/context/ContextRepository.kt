package com.example.aicourse.domain.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

interface ContextRepository {

    suspend fun summarizeContext(
        messageHistory: List<Message>,
        existContextSummary: ContextSummaryInfo? = null
    ): ContextSummaryInfo
}