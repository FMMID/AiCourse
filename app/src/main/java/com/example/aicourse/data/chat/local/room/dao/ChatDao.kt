package com.example.aicourse.data.chat.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.aicourse.data.chat.local.room.entity.AiMessageEntity
import com.example.aicourse.data.chat.local.room.entity.ChatEntity
import com.example.aicourse.data.chat.local.room.entity.ChatMessageEntity

/**
 * DAO (Data Access Object) для работы с Room БД чатов
 *
 * Предоставляет:
 * - CRUD операции для ChatEntity, ChatMessageEntity, AiMessageEntity
 * - Транзакционные методы для атомарного сохранения/загрузки состояния чата
 * - Гранулярные методы обновления (updateSettings)
 */
@Dao
interface ChatDao {

    // ========== Chat CRUD ==========

    /**
     * Вставить или обновить чат
     * OnConflictStrategy.REPLACE перезаписывает существующий чат с таким же chatId
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    /**
     * Получить чат по ID
     * @return ChatEntity или null если чат не найден
     */
    @Query("SELECT * FROM chats WHERE chat_id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    /**
     * Обновить настройки чата
     * Используется для гранулярного обновления только settings без изменения сообщений
     */
    @Query("UPDATE chats SET settings_json = :settingsJson, updated_at = :updatedAt WHERE chat_id = :chatId")
    suspend fun updateSettings(chatId: String, settingsJson: String, updatedAt: Long)

    /**
     * Обновить контекстную информацию чата
     */
    @Query("UPDATE chats SET context_summary_info_json = :contextSummaryInfoJson, updated_at = :updatedAt WHERE chat_id = :chatId")
    suspend fun updateContextSummaryInfo(chatId: String, contextSummaryInfoJson: String?, updatedAt: Long)

    /**
     * Обновить активный системный промпт чата
     */
    @Query("UPDATE chats SET active_system_prompt_json = :activeSystemPromptJson, updated_at = :updatedAt WHERE chat_id = :chatId")
    suspend fun updateActiveSystemPrompt(chatId: String, activeSystemPromptJson: String, updatedAt: Long)

    /**
     * Удалить чат по ID
     * CASCADE DELETE автоматически удалит все связанные ChatMessageEntity и AiMessageEntity
     */
    @Query("DELETE FROM chats WHERE chat_id = :chatId")
    suspend fun deleteChatById(chatId: String)

    /**
     * Получить все чаты (для будущей поддержки множественных чатов)
     */
    @Query("SELECT * FROM chats ORDER BY updated_at DESC")
    suspend fun getAllChats(): List<ChatEntity>

    // ========== ChatMessage CRUD ==========

    /**
     * Вставить или обновить список сообщений UI чата
     * OnConflictStrategy.REPLACE перезаписывает существующие сообщения с теми же messageId
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    /**
     * Вставить одно сообщение UI чата
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    /**
     * Получить все сообщения UI чата по chatId в хронологическом порядке
     */
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    suspend fun getChatMessagesByChatId(chatId: String): List<ChatMessageEntity>

    /**
     * Удалить все сообщения UI чата
     * Используется в clearHistory() и в bulk save транзакции
     */
    @Query("DELETE FROM chat_messages WHERE chat_id = :chatId")
    suspend fun deleteAllChatMessages(chatId: String)

    /**
     * Удалить одно сообщение UI чата по ID
     */
    @Query("DELETE FROM chat_messages WHERE message_id = :messageId")
    suspend fun deleteChatMessageById(messageId: String)

    // ========== AiMessage CRUD ==========

    /**
     * Вставить или обновить список сообщений для AI
     * OnConflictStrategy.REPLACE перезаписывает существующие сообщения с теми же messageId
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessages(messages: List<AiMessageEntity>)

    /**
     * Вставить одно сообщение для AI
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity)

    /**
     * Получить все сообщения для AI по chatId в хронологическом порядке
     */
    @Query("SELECT * FROM ai_messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    suspend fun getAiMessagesByChatId(chatId: String): List<AiMessageEntity>

    /**
     * Удалить все сообщения для AI
     * Используется в clearHistory() и в bulk save транзакции
     */
    @Query("DELETE FROM ai_messages WHERE chat_id = :chatId")
    suspend fun deleteAllAiMessages(chatId: String)

    /**
     * Удалить одно сообщение для AI по ID
     */
    @Query("DELETE FROM ai_messages WHERE message_id = :messageId")
    suspend fun deleteAiMessageById(messageId: String)

    // ========== Транзакции ==========

    /**
     * Атомарно сохранить полное состояние чата
     *
     * Логика:
     * 1. Вставить/обновить ChatEntity
     * 2. Удалить все старые сообщения
     * 3. Вставить новые сообщения
     *
     * @Transaction гарантирует что все операции выполнятся атомарно
     * Если любая операция провалится, все откатится
     */
    @Transaction
    suspend fun saveChatState(
        chat: ChatEntity,
        chatMessages: List<ChatMessageEntity>,
        aiMessages: List<AiMessageEntity>
    ) {
        insertChat(chat)
        deleteAllChatMessages(chat.chatId)
        deleteAllAiMessages(chat.chatId)
        if (chatMessages.isNotEmpty()) {
            insertChatMessages(chatMessages)
        }
        if (aiMessages.isNotEmpty()) {
            insertAiMessages(aiMessages)
        }
    }

    /**
     * Атомарно загрузить полное состояние чата
     *
     * @return ChatStateData или null если чат не найден
     */
    @Transaction
    suspend fun loadChatState(chatId: String): ChatStateData? {
        val chat = getChatById(chatId) ?: return null
        val chatMessages = getChatMessagesByChatId(chatId)
        val aiMessages = getAiMessagesByChatId(chatId)
        return ChatStateData(chat, chatMessages, aiMessages)
    }
}

/**
 * Data class для возврата полного состояния чата из транзакции loadChatState
 */
data class ChatStateData(
    val chat: ChatEntity,
    val chatMessages: List<ChatMessageEntity>,
    val aiMessages: List<AiMessageEntity>
)
