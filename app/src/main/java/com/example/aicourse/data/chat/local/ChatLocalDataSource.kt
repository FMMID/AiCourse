package com.example.aicourse.data.chat.local

/**
 * Интерфейс для локального источника данных чата
 * Может быть реализован через SharedPreferences, Room Database, или другое хранилище
 */
interface ChatLocalDataSource {

    /**
     * Получает историю сообщений из локального хранилища
     */
    suspend fun getMessageHistory(): List<String>

    /**
     * Сохраняет сообщение в локальное хранилище
     */
    suspend fun saveMessage(message: String, isUser: Boolean): Boolean

    /**
     * Очищает историю сообщений
     */
    suspend fun clearHistory()
}
