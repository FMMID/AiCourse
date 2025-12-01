package com.example.aicourse.presentation.chat

import androidx.compose.runtime.Immutable
import com.example.aicourse.domain.chat.model.Message

@Immutable
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
