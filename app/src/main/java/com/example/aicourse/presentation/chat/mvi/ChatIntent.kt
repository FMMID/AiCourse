package com.example.aicourse.presentation.chat.mvi

sealed interface ChatIntent {
    data class SendMessage(val text: String) : ChatIntent
    data object ClearHistory : ChatIntent
    data class SetRagMode(val mode: com.example.aicourse.domain.chat.model.RagMode) : ChatIntent
}