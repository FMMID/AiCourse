package com.example.aicourse.domain.chat.model

import com.example.aicourse.domain.chat.promt.SystemPrompt

data class SendToChatDataModel(
    val message: String,
    val systemPrompt: SystemPrompt<*>,
    val messageHistory: List<Message> = emptyList()
)
