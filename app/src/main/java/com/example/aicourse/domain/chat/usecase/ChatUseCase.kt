package com.example.aicourse.domain.chat.usecase

import com.example.aicourse.domain.chat.model.BotResponse
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
     * Извлекает подходящий SystemPrompt на основе ключевых слов в сообщении
     * Если совпадений нет, возвращает PlainTextPrompt (дефолтное поведение)
     *
     * @param content текст сообщения от пользователя
     * @return подходящий SystemPrompt (никогда не null, fallback на PlainTextPrompt)
     */
    private fun extractSystemPromptFromContent(content: String): SystemPrompt<*> {
        val lowercaseContent = content.lowercase()

        // TODO: Добавить здесь логику определения специфичных промптов на основе ключевых слов
        // Пример:
        // if (lowercaseContent.contains("код") || lowercaseContent.contains("программ")) {
        //     return CodeGeneratorPrompt()
        // }
        // if (lowercaseContent.contains("json") || lowercaseContent.contains("формат")) {
        //     return JsonOutputPrompt()
        // }

        // Fallback на дефолтный промпт
        return PlainTextPrompt()
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
