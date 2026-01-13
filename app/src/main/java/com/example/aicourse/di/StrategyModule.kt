package com.example.aicourse.di

import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.local.ChatLocalDataSource.Companion.MAIN_CHAT_ID
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.prompt.ragAssistant.RagAssistantPrompt
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.rag.domain.RagPipeline
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Модуль DI для стратегий чата
 *
 * БАГ #2 - ИЗОЛЯЦИЯ runBlocking:
 * - runBlocking изолирован в этом модуле
 * - TODO: Переделать на suspend factory в Koin 4.x или миграция на Dagger/Hilt
 */
val strategyModule = module {
    // ChatStateModel с изолированным runBlocking
    // ВАЖНО: runBlocking изолирован здесь (было в AppModule.kt:84-93)
    // TODO: Переделать на suspend factory в Koin 4.x или миграция на Dagger/Hilt
    single<ChatStateModel> { (ragIds: List<String>) ->
        runBlocking {
            val cachedState = get<ChatLocalDataSource>().getChatState(MAIN_CHAT_ID)
            if (ragIds.isNotEmpty()) {
                cachedState.copy(ragIds = ragIds)
            } else {
                cachedState
            }
        }
    }

    // ChatStrategy с инжектированными Tools
    factory<ChatStrategy> { (ragIds: List<String>) ->
        SimpleChatStrategy(
            initChatStateModel = get { parametersOf(ragIds) },
            initialSystemPrompt = RagAssistantPrompt(),
            ragPipeline = get<RagPipeline>(),
            // NEW: Инжектируем Tools (вместо ручного создания в SimpleChatStrategy.kt:51-75)
            contextWindowManager = get(),
            tokenCompareManager = get(),
            modelInfoManager = get(),
            mcpGitClient = get(named("mcpGitClient"))
        )
    }
}
