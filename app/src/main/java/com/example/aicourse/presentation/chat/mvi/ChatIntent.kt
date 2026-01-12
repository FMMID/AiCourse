package com.example.aicourse.presentation.chat.mvi

import com.example.aicourse.rag.domain.model.RagMode

sealed interface ChatIntent {
    data class SendMessage(val text: String) : ChatIntent
    data object ClearHistory : ChatIntent
    data class SetRagMode(val mode: RagMode) : ChatIntent
}