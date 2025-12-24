package com.example.aicourse.presentation.chat.mvi

import androidx.compose.runtime.Immutable
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt

@Immutable
//TODO переделать на другую модель
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrompt: SystemPrompt<*> = PlainTextPrompt(),
    val isRagModeEnabled: Boolean = false,
    val showRagButton: Boolean = false
)
