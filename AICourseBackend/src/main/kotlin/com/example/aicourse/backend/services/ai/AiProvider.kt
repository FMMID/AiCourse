package com.example.aicourse.backend.services.ai

import com.example.aicourse.backend.services.ai.gigaChat.GigaChatServerClient
import com.example.aicourse.backend.services.notes.AiServiceNotes

object AiProvider {

    fun getAiServiceNotes(): AiServiceNotes {
        return GigaChatServerClient
    }
}