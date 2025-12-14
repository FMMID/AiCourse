package com.example.aicourse.data.chat.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aicourse.data.chat.local.room.converters.RoomTypeConverters
import com.example.aicourse.data.chat.local.room.dao.ChatDao
import com.example.aicourse.data.chat.local.room.entity.AiMessageEntity
import com.example.aicourse.data.chat.local.room.entity.ChatEntity
import com.example.aicourse.data.chat.local.room.entity.ChatMessageEntity

/**
 * Room Database для хранения чатов и сообщений
 *
 * Содержит 3 таблицы:
 * - chats: основная информация о чате (настройки, активный промпт, контекст)
 * - chat_messages: сообщения UI чата (полная информация с typedResponse, toolResult)
 * - ai_messages: сообщения для отправки в AI API (упрощенная версия без UI данных)
 *
 * Использует RoomTypeConverters для автоматического преобразования
 * сложных типов (BotResponse, SystemPrompt, ToolResult и т.д.) в JSON строки
 *
 * Version 1 - начальная схема БД
 * exportSchema = true - Room экспортирует схему в app/schemas/ для миграций
 */
@Database(
    entities = [
        ChatEntity::class,
        ChatMessageEntity::class,
        AiMessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class ChatDatabase : RoomDatabase() {

    /**
     * Получить DAO для работы с чатами
     */
    abstract fun chatDao(): ChatDao

    companion object {
        private const val DATABASE_NAME = "chat_database.db"

        /**
         * Singleton instance БД
         * @Volatile гарантирует видимость изменений между потоками
         */
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        /**
         * Получить или создать instance БД
         *
         * Thread-safe благодаря synchronized блоку
         * Double-checked locking pattern для минимизации синхронизации
         *
         * @param context Application context (не Activity context!)
         * @return синглтон ChatDatabase
         */
        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Создать и сконфигурировать БД
         */
        private fun buildDatabase(context: Context): ChatDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ChatDatabase::class.java,
                DATABASE_NAME
            )
                // TODO: В production заменить на реальные миграции
                // fallbackToDestructiveMigration() удаляет все данные при изменении схемы
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Очистить instance (для тестов)
         * НЕ использовать в production коде
         */
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
