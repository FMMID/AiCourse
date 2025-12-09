package com.example.aicourse.presentation.chat

import androidx.compose.runtime.Immutable
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SystemPrompt
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.model.plain.PlainTextPrompt

@Immutable
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrompt: SystemPrompt<*> = PlainTextPrompt(),
    val lastTokenUsage: TokenUsage? = null,
    val lastModelName: String? = null
)
