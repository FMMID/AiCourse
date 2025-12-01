package com.example.aicourse.domain.chat.repository

/**
 * Интерфейс репозитория для работы с чатом
 * Определяет контракт взаимодействия с источниками данных (Clean Architecture)
 */
interface ChatRepository {

    /**
     * Отправляет сообщение боту и получает ответ
     * @param message текст сообщения от пользователя
     * @return ответ от бота
     * @throws Exception если произошла ошибка при отправке/получении
     */
    suspend fun sendMessage(message: String): Result<String>

    /**
     * Получает историю сообщений из локального хранилища (если есть)
     * @return список сообщений
     */
    suspend fun getMessageHistory(): Result<List<String>>

    /**
     * Сохраняет сообщение в локальное хранилище
     */
    suspend fun saveMessage(message: String, isUser: Boolean): Result<Unit>

    /**
     * Очищает историю сообщений
     */
    suspend fun clearHistory(): Result<Unit>
}
