package com.example.aicourse.presentation.chat

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.aicourse.di.AppInjector
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.usecase.ChatUseCase
import com.example.aicourse.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    application: Application,
    private val chatUseCase: ChatUseCase = AppInjector.createChatUseCase(application),
) : BaseViewModel<ChatUiState, ChatIntent>(application, ChatUiState()) {

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> sendMessage(intent.text)
            is ChatIntent.ClearHistory -> clearHistory()
            is ChatIntent.ResetPrompt -> resetPrompt()
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        if (isResetCommand(text)) {
            resetPrompt()
            return
        }

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            type = MessageType.USER
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            chatUseCase.sendMessageToBot(userMessage = userMessage)
                .onSuccess { complexBotMessage ->
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + complexBotMessage.message,
                            isLoading = false,
                            error = null,
                            activePrompt = complexBotMessage.systemPrompt,
                            toolResult = complexBotMessage.toolResult,
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = "Ошибка: ${exception.message}"
                        )
                    }
                }
        }
    }

    private fun isResetCommand(text: String): Boolean {
        val lowerText = text.trim().lowercase()
        return lowerText == "/reset" || lowerText == "/plain"
    }

    private fun clearHistory() {
        viewModelScope.launch {
            chatUseCase.clearChatHistory()
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            messages = emptyList(),
                            error = null,
                            activePrompt = PlainTextPrompt()
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(
                            error = "Ошибка очистки истории: ${exception.message}"
                        )
                    }
                }
        }
    }

    private fun resetPrompt() {
        _uiState.update { state ->
            state.copy(
                activePrompt = PlainTextPrompt()
            )
        }
    }
}
