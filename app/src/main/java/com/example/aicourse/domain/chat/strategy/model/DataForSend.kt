package com.example.aicourse.domain.chat.strategy.model

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.promt.SystemPrompt

sealed interface DataForSend {

    class LocalResponse(
        val responseMessage: Message,
        val activePrompt: SystemPrompt<*>,
        val activeModelName: String? = null
    ) : DataForSend

    class RemoteCall(
        val sendToChatDataModel: SendToChatDataModel
    ) : DataForSend
}