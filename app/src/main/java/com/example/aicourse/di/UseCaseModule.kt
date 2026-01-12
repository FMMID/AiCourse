package com.example.aicourse.di

import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.chat.usecase.SetRagModelUseCase
import com.example.aicourse.domain.settings.usecase.GetLocalMcpToolsUseCase
import com.example.aicourse.domain.settings.usecase.SettingsChatUseCase
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Модуль DI для Use Cases
 *
 * ИСПРАВЛЕНИЕ БАГА #1:
 * - Зарегистрированы SettingsChatUseCase и GetLocalMcpToolsUseCase
 *   (ранее использовались в SettingsViewModel, но не были зарегистрированы в AppModule.kt:154-155)
 */
val useCaseModule = module {
    // Settings Use Cases
    // БАГ #1 ИСПРАВЛЕН - эти UseCases теперь зарегистрированы!
    single<SettingsChatUseCase> {
        SettingsChatUseCase()
    }

    single<GetLocalMcpToolsUseCase> {
        GetLocalMcpToolsUseCase(mcpRepositoryImp = get())
    }

    // Chat Use Cases (требуют ragIds)
    factory<SendMessageChatUseCase> { (ragIds: List<String>) ->
        SendMessageChatUseCase(
            chatRepository = get { parametersOf(ragIds) },
            chatStrategy = get { parametersOf(ragIds) }
        )
    }

    factory<ClearHistoryChatUseCase> { (ragIds: List<String>) ->
        ClearHistoryChatUseCase(
            chatRepository = get { parametersOf(ragIds) },
            chatStrategy = get { parametersOf(ragIds) }
        )
    }

    factory<GetHistoryChatUseCase> { (ragIds: List<String>) ->
        GetHistoryChatUseCase(
            chatRepository = get { parametersOf(ragIds) }
        )
    }

    factory<SetRagModelUseCase> { (ragIds: List<String>) ->
        SetRagModelUseCase(
            chatStrategy = get { parametersOf(ragIds) }
        )
    }
}
