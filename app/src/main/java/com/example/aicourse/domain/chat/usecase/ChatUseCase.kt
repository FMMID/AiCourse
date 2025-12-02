package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.JsonOutputPrompt
import com.example.aicourse.domain.chat.model.PlainTextPrompt
import com.example.aicourse.domain.chat.model.SystemPrompt
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
     * Автоматически определяет подходящий SystemPrompt на основе содержимого сообщения
     * @param message текст сообщения от пользователя
     * @return Result с ответом бота или ошибкой
     */
    suspend fun sendMessageToBot(message: String): Result<String> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }

        val systemPrompt = extractSystemPromptFromContent(message)
        val result = chatRepository.sendMessage(message, systemPrompt)

        return result.map { botResponse ->
            botResponse.rawContent
        }
    }

    /**
     * Извлекает подходящий SystemPrompt на основе триггеров в сообщении
     * Проходит по списку доступных промптов и возвращает первый подошедший
     * Если совпадений нет, возвращает PlainTextPrompt (дефолтное поведение)
     *
     * @param content текст сообщения от пользователя
     * @return подходящий SystemPrompt (никогда не null, fallback на PlainTextPrompt)
     */
    private fun extractSystemPromptFromContent(content: String): SystemPrompt<*> {
        val availablePrompts = listOf(
            JsonOutputPrompt()
        )

        return availablePrompts.firstOrNull { prompt ->
            prompt.matches(content)
        } ?: PlainTextPrompt()
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
