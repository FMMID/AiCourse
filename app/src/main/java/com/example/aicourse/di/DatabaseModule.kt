package com.example.aicourse.di

import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.local.RoomChatLocalDataSource
import com.example.aicourse.data.chat.local.room.ChatDatabase
import com.example.aicourse.data.chat.local.room.dao.ChatDao
import com.example.aicourse.data.chat.local.room.mapper.ChatStateMapper
import com.example.aicourse.prompt.ragAssistant.RagAssistantPrompt
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Модуль DI для компонентов базы данных
 * - Room Database
 * - DAO
 * - Local Data Sources
 * - Mappers
 */
val databaseModule = module {
    // Database
    single<ChatDatabase> {
        ChatDatabase.getInstance(androidContext())
    }

    // DAO
    single<ChatDao> {
        get<ChatDatabase>().chatDao()
    }

    // Mapper
    factory<ChatStateMapper> {
        ChatStateMapper()
    }

    // Local Data Source
    single<ChatLocalDataSource> {
        RoomChatLocalDataSource(
            chatDao = get(),
            mapper = get(),
            initActiveUserPrompt = RagAssistantPrompt() // Было глобальной переменной в AppModule.kt:44
        )
    }
}
