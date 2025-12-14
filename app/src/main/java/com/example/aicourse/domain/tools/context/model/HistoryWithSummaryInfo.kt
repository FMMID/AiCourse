package com.example.aicourse.domain.tools.context.model

import com.example.aicourse.domain.chat.model.Message

data class HistoryWithSummaryInfo(
    val messagesForSendToAi: List<Message>,
    val contextSummaryInfo: ContextSummaryInfo?
)