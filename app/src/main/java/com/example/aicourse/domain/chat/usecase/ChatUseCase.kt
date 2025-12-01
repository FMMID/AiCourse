package com.example.aicourse.domain.chat.usecase

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
     * @param message текст сообщения от пользователя
     * @return Result с ответом бота или ошибкой
     */
    suspend fun sendMessageToBot(message: String): Result<String> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }

        return chatRepository.sendMessage(message)
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
