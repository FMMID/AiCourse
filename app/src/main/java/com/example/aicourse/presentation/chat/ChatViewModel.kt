package com.example.aicourse.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.BuildConfig
import com.example.aicourse.data.chat.local.InMemoryChatDataSource
import com.example.aicourse.data.chat.remote.GigaChatDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.usecase.ChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatUseCase: ChatUseCase = createChatUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: ChatIntent) {
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
            chatUseCase.sendMessageToBot(text, _uiState.value.activePrompt)
                .onSuccess { chatResponse ->
                    val botMessage = Message(
                        id = UUID.randomUUID().toString(),
                        text = chatResponse.botResponse.rawContent,
                        type = MessageType.BOT,
                        typedResponse = chatResponse.botResponse
                    )

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + botMessage,
                            isLoading = false,
                            error = null,
                            activePrompt = chatResponse.newPrompt
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

    companion object {
        /**
         * Фабричная функция для создания ChatUseCase с зависимостями
         * TODO: Заменить на Dependency Injection (Hilt, Koin, и т.д.)
         */
        private fun createChatUseCase(): ChatUseCase {
            val authorizationKey = BuildConfig.GIGACHAT_AUTH_KEY
            val remoteDataSource = GigaChatDataSource(authorizationKey)
            val localDataSource = InMemoryChatDataSource()
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource)
            return ChatUseCase(repository)
        }
    }
}
