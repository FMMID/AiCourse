package com.example.aicourse.domain.chat.strategy.model

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

sealed interface DataForSend {

    val activePrompt: SystemPrompt<*>

    class LocalResponse(
        override val activePrompt: SystemPrompt<*>,
        val responseMessage: Message?,
    ) : DataForSend

    class RemoteCall(
        override val activePrompt: SystemPrompt<*>,
        val messageHistory: List<Message> = emptyList(),
        val contextSummaryInfo: ContextSummaryInfo? = null,
    ) : DataForSend
}
