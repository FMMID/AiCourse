package com.example.aicourse.presentation.chat.mvi

import androidx.compose.runtime.Immutable
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.rag.domain.model.RagMode
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.prompt.plain.PlainTextPrompt

@Immutable
//TODO переделать на другую модель
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrompt: SystemPrompt<*> = PlainTextPrompt(),
    val ragMode: RagMode = RagMode.DISABLED,
    val showRagButton: Boolean = false
)
