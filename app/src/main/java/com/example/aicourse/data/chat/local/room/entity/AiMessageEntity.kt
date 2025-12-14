package com.example.aicourse.data.chat.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity для хранения сообщений отправляемых в AI API (messagesForSendToAi)
 *
 * Упрощенная версия ChatMessageEntity - содержит только данные необходимые для AI:
 * - Текст сообщения
 * - Тип (USER, BOT, SYSTEM)
 * - Использование токенов (TokenUsage) - JSON
 *
 * НЕ содержит:
 * - typedResponse (не нужно для отправки в API)
 * - toolResult (не нужно для отправки в API)
 *
 * Foreign Key:
 * - chatId → ChatEntity.chatId с CASCADE DELETE
 *
 * Index:
 * - (chat_id, timestamp) для быстрых хронологических запросов
 *
 * Дублирование данных:
 * Одно и то же сообщение может присутствовать как в chat_messages, так и в ai_messages
 * с одинаковым message_id. Это осознанное дублирование для разделения UI и API истории.
 */
@Entity(
    tableName = "ai_messages",
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
data class AiMessageEntity(
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
     * Использование токенов в формате JSON
     * Может быть null если информация недоступна
     */
    @ColumnInfo(name = "token_usage_json")
    val tokenUsageJson: String?
)
