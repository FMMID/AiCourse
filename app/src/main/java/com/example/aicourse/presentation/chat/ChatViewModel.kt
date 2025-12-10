package com.example.aicourse.presentation.chat

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.aicourse.BuildConfig
import com.example.aicourse.data.chat.local.InMemoryChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.usecase.ChatUseCase
import com.example.aicourse.domain.chat.util.TokenStatisticsCalculator
import com.example.aicourse.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    application: Application,
    private val chatUseCase: ChatUseCase = createChatUseCase(application)
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
            chatUseCase.sendMessageToBot(
                message = text,
                currentPrompt = _uiState.value.activePrompt,
                messageHistory = _uiState.value.messages
            )
                .onSuccess { chatResponse ->
                    val previousBotMessage = _uiState.value.messages
                        .asReversed()
                        .firstOrNull { it.type == MessageType.BOT && it.tokenUsage?.hasData() == true }

                    val diff = TokenStatisticsCalculator.calculateDiff(
                        chatResponse.tokenUsage,
                        previousBotMessage
                    )

                    val botMessage = Message(
                        id = UUID.randomUUID().toString(),
                        text = chatResponse.botResponse.rawContent,
                        type = MessageType.BOT,
                        typedResponse = chatResponse.botResponse,
                        tokenUsage = chatResponse.tokenUsage,
                        contextLimit = chatResponse.newPrompt.maxTokens,
                        tokenUsageDiff = diff
                    )

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + botMessage,
                            isLoading = false,
                            error = null,
                            activePrompt = chatResponse.newPrompt,
                            lastTokenUsage = chatResponse.tokenUsage,
                            lastModelName = chatResponse.modelName
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
        private fun createChatUseCase(application: Application): ChatUseCase {
            val authorizationKey = BuildConfig.HUGGING_FACE_AUTH_KEY
            val remoteDataSource = HuggingFaceDataSource(authorizationKey)
            val localDataSource = InMemoryChatDataSource()
            val repository = ChatRepositoryImpl(application, remoteDataSource, localDataSource)
            return ChatUseCase(repository)
        }
    }
}
