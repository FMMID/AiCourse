package com.example.aicourse.presentation.chat

sealed interface ChatIntent {
    data class SendMessage(val text: String) : ChatIntent
    data object ClearHistory : ChatIntent
    data object ResetPrompt : ChatIntent
}
