package com.example.aicourse.data.chat.local

import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.settings.model.SettingsChatModel

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

    /**
     * Гранулярное обновление только настроек чата
     * Позволяет изменить ApiImplementation, HistoryStrategy или OutPutDataStrategy
     * без перезаписи всех сообщений
     *
     * @param chatId идентификатор чата
     * @param settings новые настройки
     */
    suspend fun updateSettings(chatId: String, settings: SettingsChatModel)
}
