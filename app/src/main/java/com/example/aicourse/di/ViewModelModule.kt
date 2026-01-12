package com.example.aicourse.di

import android.app.Application
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.settings.mvi.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Модуль DI для ViewModels
 * - SettingsViewModel (теперь с зарегистрированными UseCases!)
 * - ChatViewModel
 */
val viewModelModule = module {
    // Settings ViewModel
    viewModel {
        SettingsViewModel(
            application = androidContext() as Application,
            settingsChatUseCase = get(), // Теперь зарегистрирован в UseCaseModule!
            getLocalMcpToolsUseCase = get() // Теперь зарегистрирован в UseCaseModule!
        )
    }

    // Chat ViewModel
    viewModel { (chatId: String, ragIdsString: String?) ->
        val ragIds = ragIdsString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        ChatViewModel(
            application = androidContext() as Application,
            chatId = chatId,
            ragIdsString = ragIdsString,
            sendMessageChatUseCase = get { parametersOf(ragIds) },
            clearHistoryChatUseCase = get { parametersOf(ragIds) },
            getHistoryChatUseCase = get { parametersOf(ragIds) },
            setRagModelUseCase = get { parametersOf(ragIds) }
        )
    }
}
