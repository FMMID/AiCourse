package com.example.aicourse.data.chat.local

import com.example.aicourse.domain.chat.model.ChatStateModel

/**
 * Интерфейс для локального источника данных чата
 * Может быть реализован через SharedPreferences, Room Database, или другое хранилище
 */
interface ChatLocalDataSource {

    companion object {
        const val MAIN_CHAT_ID = "main_chat_id"
    }

    /**
     * Получает историю сообщений из локального хранилища
     */
    suspend fun getChatState(id: String): ChatStateModel

    /**
     * Сохранение всего состояния чата
     */
    suspend fun saveChatState(chatStateModel: ChatStateModel)

    /**
     * Очищает историю сообщений
     */
    suspend fun clearHistory()
}
