package com.example.aicourse.data.chat.local

import com.example.aicourse.data.chat.local.room.converters.PolymorphicJson
import com.example.aicourse.data.chat.local.room.dao.ChatDao
import com.example.aicourse.data.chat.local.room.mapper.ChatStateMapper
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.RagMode
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация ChatLocalDataSource с использованием Room Database
 *
 * Предоставляет персистентное хранилище для состояния чата
 * Заменяет InMemoryChatDataSource для сохранения данных между запусками приложения
 *
 * @param chatDao DAO для доступа к Room БД
 * @param mapper конвертер между domain моделями и Room entities
 */
class RoomChatLocalDataSource(
    private val chatDao: ChatDao,
    private val mapper: ChatStateMapper,
    private val initActiveUserPrompt: SystemPrompt<*>
) : ChatLocalDataSource {

    private val json = PolymorphicJson.instance

    /**
     * Получает состояние чата из БД
     *
     * Логика:
     * - Если чат существует в БД → загружаем и конвертируем в domain модель
     * - Если чата нет → создаём новый ChatStateModel с дефолтными настройками
     *
     * @param id идентификатор чата (по умолчанию MAIN_CHAT_ID)
     * @return ChatStateModel - всегда возвращаем непустой объект
     */
    override suspend fun getChatState(id: String): ChatStateModel = withContext(Dispatchers.IO) {
        val data = chatDao.loadChatState(id) ?: return@withContext createDefaultChatState(id)

        // Конвертируем entities → domain модель
        mapper.toDomainModel(data)
    }

    /**
     * Сохраняет полное состояние чата в БД
     *
     * Использует атомарную транзакцию saveChatState() из DAO
     * Перезаписывает все сообщения (bulk save pattern)
     *
     * @param chatStateModel domain модель состояния чата
     */
    override suspend fun saveChatState(chatStateModel: ChatStateModel) = withContext(Dispatchers.IO) {
        val (chatEntity, chatMessages, aiMessages) = mapper.toEntityModels(chatStateModel)
        chatDao.saveChatState(chatEntity, chatMessages, aiMessages)
    }

    /**
     * Очищает историю сообщений (удаляет только сообщения, сохраняет настройки)
     *
     * Реализация:
     * - Удаляем все ChatMessageEntity
     * - Удаляем все AiMessageEntity
     * - ChatEntity остаётся с настройками и activePrompt
     */
    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatDao.deleteAllChatMessages(ChatLocalDataSource.MAIN_CHAT_ID)
        chatDao.deleteAllAiMessages(ChatLocalDataSource.MAIN_CHAT_ID)
    }

    /**
     * Гранулярное обновление только настроек чата (без изменения сообщений)
     *
     * Используется когда нужно обновить ApiImplementation, HistoryStrategy или OutPutDataStrategy
     * без перезаписи всей истории сообщений
     *
     * @param chatId идентификатор чата
     * @param settings новые настройки
     */
    override suspend fun updateSettings(chatId: String, settings: SettingsChatModel) = withContext(Dispatchers.IO) {
        val settingsJson = json.encodeToString(settings)
        chatDao.updateSettings(chatId, settingsJson, System.currentTimeMillis())
    }

    /**
     * Создаёт дефолтное состояние чата (когда чата ещё нет в БД)
     *
     * Совпадает с дефолтными настройками из InMemoryChatDataSource
     */
    private fun createDefaultChatState(id: String): ChatStateModel {
        return ChatStateModel(
            id = id,
            settingsChatModel = SettingsChatModel(
                currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
                historyStrategy = HistoryStrategy.PLAIN,
                outPutDataStrategy = OutPutDataStrategy.None
            ),
            chatMessages = mutableListOf(),
            messagesForSendToAi = mutableListOf(),
            contextSummaryInfo = null,
            activeSystemPrompt = initActiveUserPrompt,
            ragIds = emptyList(),
            ragMode = RagMode.DISABLED,
        )
    }
}
