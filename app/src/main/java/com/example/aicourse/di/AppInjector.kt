package com.example.aicourse.di

import android.app.Application
import android.content.Context
import com.example.aicourse.data.chat.local.InMemoryChatDataSource
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.data.tools.context.ContextRepositoryImp
import com.example.aicourse.data.tools.context.SummarizeContextDataSource
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.usecase.SettingsChatUseCase
import com.example.aicourse.domain.tools.context.ContextRepository

// TODO Заменить на Dependency Injection (Hilt, Koin, и т.д.)
object AppInjector {

    val existDataSources: MutableMap<ApiImplementation, BaseChatRemoteDataSource> = mutableMapOf()
    var settingsChatUseCase: SettingsChatUseCase? = null

    fun createContextRepository(settingsChatModel: SettingsChatModel): ContextRepository {
        val summarizeContextDataSource: SummarizeContextDataSource =
            when (val apiImplementation = settingsChatModel.currentUseApiImplementation) {
                ApiImplementation.GIGA_CHAT -> {
                    existDataSources.getOrPut(ApiImplementation.GIGA_CHAT) { GigaChatDataSource(apiImplementation.key) }
                }

                ApiImplementation.HUGGING_FACE -> {
                    existDataSources.getOrPut(ApiImplementation.HUGGING_FACE) { HuggingFaceDataSource(apiImplementation.key) }
                }
            }

        return ContextRepositoryImp(summarizeContextDataSource)
    }

    fun createChatRepository(context: Context, settingsChatModel: SettingsChatModel): ChatRepository {
        val remoteDataSource = when (val apiImplementation = settingsChatModel.currentUseApiImplementation) {
            ApiImplementation.GIGA_CHAT -> {
                existDataSources.getOrPut(ApiImplementation.GIGA_CHAT) { GigaChatDataSource(apiImplementation.key) }
            }

            ApiImplementation.HUGGING_FACE -> {
                existDataSources.getOrPut(ApiImplementation.HUGGING_FACE) { HuggingFaceDataSource(apiImplementation.key) }
            }
        }

        return ChatRepositoryImpl(context, remoteDataSource, InMemoryChatDataSource())
    }

    fun createSettingsChatUseCase(): SettingsChatUseCase {
        return settingsChatUseCase ?: run {
            val newSettingsChatUseCase = SettingsChatUseCase()
            settingsChatUseCase = newSettingsChatUseCase
            newSettingsChatUseCase
        }
    }

    fun createChatUseCase(application: Application): ChatUseCase {
        val settingsChatModel = createSettingsChatUseCase().getSettingsChatModel()
        val repository = createChatRepository(application, settingsChatModel)
        val simpleDataForSendStrategyImp = SimpleChatStrategy(initSettingsChatModel = settingsChatModel)

        return ChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
    }
}