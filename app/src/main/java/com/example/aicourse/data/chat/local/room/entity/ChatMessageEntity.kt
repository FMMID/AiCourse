package com.example.aicourse.data.chat.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity для хранения сообщений UI чата (chatMessages)
 *
 * Содержит полную информацию о сообщении:
 * - Текст сообщения
 * - Тип (USER, BOT, SYSTEM)
 * - Типизированный ответ бота (BotResponse) - полиморфный JSON
 * - Использование токенов (TokenUsage) - JSON
 * - Результат инструмента (ToolResult) - полиморфный JSON
 *
 * Foreign Key:
 * - chatId → ChatEntity.chatId с CASCADE DELETE
 *
 * Index:
 * - (chat_id, timestamp) для быстрых хронологических запросов
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chat_id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chat_id", "timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "message_id")
    val messageId: String,

    /**
     * ID чата (foreign key → chats.chat_id)
     */
    @ColumnInfo(name = "chat_id")
    val chatId: String,

    /**
     * Текст сообщения
     */
    @ColumnInfo(name = "text")
    val text: String,

    /**
     * Тип сообщения: "USER", "BOT", "SYSTEM"
     */
    @ColumnInfo(name = "type")
    val type: String,

    /**
     * Unix timestamp в миллисекундах
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    /**
     * Типизированный ответ бота в формате JSON
     * Полиморфный тип: PlainTextResponse, JsonOutputResponse, PcBuildResponse и т.д.
     * Может быть null для USER сообщений
     */
    @ColumnInfo(name = "typed_response_json")
    val typedResponseJson: String?,

    /**
     * Использование токенов в формате JSON
     * Может быть null если информация недоступна
     */
    @ColumnInfo(name = "token_usage_json")
    val tokenUsageJson: String?,

    /**
     * Результат инструмента в формате JSON
     * Полиморфный тип: ContextWindowInfo, TokenUsageDiff, ModelInfo и т.д.
     * Может быть null если инструмент не использовался
     */
    @ColumnInfo(name = "tool_result_json")
    val toolResultJson: String?
)
