package com.example.aicourse.data.tools.context

import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

interface SummarizeContextDataSource {

    suspend fun summarizeContext(messageHistory: String): ContextSummaryInfo
}