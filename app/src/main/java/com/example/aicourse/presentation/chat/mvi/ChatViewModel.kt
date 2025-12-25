package com.example.aicourse.presentation.chat.mvi

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.RagMode
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.chat.usecase.SetRagModelUseCase
import com.example.aicourse.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    application: Application,
    ragIdsString: String?,
    private val chatId: String,
    private val sendMessageChatUseCase: SendMessageChatUseCase,
    private val clearHistoryChatUseCase: ClearHistoryChatUseCase,
    private val getHistoryChatUseCase: GetHistoryChatUseCase,
    private val setRagModelUseCase: SetRagModelUseCase
) : BaseViewModel<ChatUiState, ChatIntent>(application, ChatUiState()) {

    init {
        viewModelScope.launch {
            getHistoryChatUseCase(input = chatId).onSuccess { chatStateModel ->
                _uiState.update {
                    ChatUiState(
                        messages = chatStateModel.chatMessages,
                        isLoading = false,
                        error = null,
                        activePrompt = chatStateModel.activeSystemPrompt,
                        ragMode = chatStateModel.ragMode,
                        showRagButton = !ragIdsString.isNullOrBlank()
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> sendMessage(intent.text)
            is ChatIntent.ClearHistory -> clearHistory()
            is ChatIntent.SetRagMode -> changeRagMode(intent.mode)
        }
    }

    private fun changeRagMode(newMode: RagMode) {
        if (_uiState.value.ragMode == newMode) return
        _uiState.update { it.copy(ragMode = newMode) }
        viewModelScope.launch { setRagModelUseCase(newMode) }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

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
            sendMessageChatUseCase(input = userMessage)
                .onSuccess { complexBotMessage ->
                    val messages = if (complexBotMessage.message != null) {
                        _uiState.value.messages + complexBotMessage.message
                    } else {
                        _uiState.value.messages
                    }
                    _uiState.update { state ->
                        state.copy(
                            messages = messages,
                            isLoading = false,
                            error = null,
                            activePrompt = complexBotMessage.systemPrompt,
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

    private fun clearHistory() {
        viewModelScope.launch {
            clearHistoryChatUseCase(chatId)
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
}
