package com.example.aicourse.domain.chat.strategy.model

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt

sealed interface DataForSend {

    val activePrompt: SystemPrompt<*>

    class LocalResponse(
        val responseMessage: Message,
        override val activePrompt: SystemPrompt<*>,
    ) : DataForSend

    class RemoteCall(
        val messageHistory: List<Message> = emptyList(),
        override val activePrompt: SystemPrompt<*>
    ) : DataForSend
}
