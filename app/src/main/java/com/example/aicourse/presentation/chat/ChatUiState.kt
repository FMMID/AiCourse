package com.example.aicourse.presentation.chat

import androidx.compose.runtime.Immutable
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.tools.ToolResult

@Immutable
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrompt: SystemPrompt<*> = PlainTextPrompt(),
    val toolResult: ToolResult? = null,
)
