package com.example.aicourse.domain.tools.context

import com.example.aicourse.domain.chat.model.Message

interface ContextRepository {

    suspend fun summarizeContext(messageHistory: List<Message>): String
}