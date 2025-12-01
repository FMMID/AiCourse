package com.example.aicourse.presentation.chat

import com.example.aicourse.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
