package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.BotResponse
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SystemPrompt
import com.example.aicourse.domain.chat.model.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.model.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository

/**
 * Use Case для работы с чатом
 * Содержит бизнес-логику отправки сообщений
 * Зависит только от интерфейса ChatRepository (Clean Architecture)
 */
class ChatUseCase(
    private val chatRepository: ChatRepository
) {

    /**
     * Отправляет сообщение боту через репозиторий
     * Определяет промпт на основе триггеров или использует текущий активный
     * @param message текст сообщения от пользователя
     * @param currentPrompt текущий активный промпт
     * @param messageHistory история предыдущих сообщений для контекста
     * @return Result с типизированным ответом и новым промптом
     */
    suspend fun sendMessageToBot(
        message: String,
        currentPrompt: SystemPrompt<*>,
        messageHistory: List<Message> = emptyList()
    ): Result<ChatResponse> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        if (isResetCommand(message)) {
            return Result.failure(IllegalArgumentException("Используйте ResetPrompt intent для сброса"))
        }
        val newPrompt = extractSystemPromptFromContent(message) ?: currentPrompt
        val result = chatRepository.sendMessage(message, newPrompt, messageHistory)
        return result.map { botResponse ->
            ChatResponse(
                botResponse = botResponse,
                newPrompt = newPrompt
            )
        }
    }

    /**
     * Проверяет команды сброса промпта
     */
    private fun isResetCommand(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()
        return lowerMessage == "/reset" || lowerMessage == "/plain"
    }

    /**
     * Извлекает подходящий SystemPrompt на основе триггеров в сообщении
     * Проходит по списку доступных промптов и возвращает первый подошедший
     *
     * @param content текст сообщения от пользователя
     * @return подходящий SystemPrompt или null если триггеров не найдено
     */
    private fun extractSystemPromptFromContent(content: String): SystemPrompt<*>? {
        val availablePrompts = listOf(
            JsonOutputPrompt(),
            BuildComputerAssistantPrompt()
        )

        return availablePrompts.firstOrNull { prompt ->
            prompt.matches(content)
        }
    }

    /**
     * Очищает историю чата
     */
    suspend fun clearChatHistory(): Result<Unit> {
        return chatRepository.clearHistory()
    }

    /**
     * Получает историю сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>> {
        return chatRepository.getMessageHistory()
    }
}

/**
 * Результат отправки сообщения с типизированным ответом и новым промптом
 */
data class ChatResponse(
    val botResponse: BotResponse,
    val newPrompt: SystemPrompt<*>
)
