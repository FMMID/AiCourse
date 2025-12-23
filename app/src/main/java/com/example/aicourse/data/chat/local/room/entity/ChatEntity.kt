package com.example.aicourse.data.chat.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity для хранения основной информации о чате
 *
 * Содержит:
 * - Настройки чата (SettingsChatModel) как JSON
 * - Информацию о контексте (ContextSummaryInfo) как JSON
 * - Активный системный промпт (SystemPrompt<*>) как JSON с type discriminator
 * - Метаданные (createdAt, updatedAt)
 *
 * Связи:
 * - ChatMessageEntity (1:N) через chatId foreign key
 * - AiMessageEntity (1:N) через chatId foreign key
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    val chatId: String,

    /**
     * Настройки чата в формате JSON
     * Содержит: ApiImplementation, HistoryStrategy, OutPutDataStrategy
     */
    @ColumnInfo(name = "settings_json")
    val settingsJson: String,

    /**
     * Информация о контексте в формате JSON
     * Может быть null если не используется SUMMARIZE стратегия
     */
    @ColumnInfo(name = "context_summary_info_json")
    val contextSummaryInfoJson: String?,

    /**
     * Активный системный промпт в формате JSON с type discriminator
     * Полиморфный тип: PlainTextPrompt, JsonOutputPrompt, DynamicTemperaturePrompt и т.д.
     */
    @ColumnInfo(name = "active_system_prompt_json")
    val activeSystemPromptJson: String,

    /**
     * Время создания чата (Unix timestamp в миллисекундах)
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Время последнего обновления (Unix timestamp в миллисекундах)
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "rag_index_id")
    val ragIndexId: String?
)
