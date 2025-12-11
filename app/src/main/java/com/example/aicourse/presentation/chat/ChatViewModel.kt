package com.example.aicourse.presentation.chat

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.aicourse.data.chat.local.InMemoryChatDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.usecase.SettingsChatUseCase
import com.example.aicourse.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    application: Application,
    private val settingsChatUseCase: SettingsChatUseCase = createSettingsUseCase(),
    private val chatUseCase: ChatUseCase = createChatUseCase(application, settingsChatUseCase),
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

    companion object {
        /**
         * Фабричная функция для создания ChatUseCase с зависимостями
         * TODO: Заменить на Dependency Injection (Hilt, Koin, и т.д.)
         */
        private fun createChatUseCase(application: Application, settingsChatUseCase: SettingsChatUseCase): ChatUseCase {
            val settingsChatModel = settingsChatUseCase.getSettingsChatModel()
            val remoteDataSource = when (val apiImplementation = settingsChatModel.currentUseApiImplementation) {
                ApiImplementation.GIGA_CHAT -> GigaChatDataSource(authorizationKey = apiImplementation.key)
                ApiImplementation.HUGGING_FACE -> HuggingFaceDataSource(apiToken = apiImplementation.key)
            }
            val localDataSource = InMemoryChatDataSource()
            val repository = ChatRepositoryImpl(
                context = application,
                remoteDataSource = remoteDataSource,
                localDataSource = localDataSource
            )
            val simpleDataForSendStrategyImp = SimpleChatStrategy(initSettingsChatModel = settingsChatModel)
            return ChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
        }

        private fun createSettingsUseCase(): SettingsChatUseCase {
            return SettingsChatUseCase()
        }
    }
}
