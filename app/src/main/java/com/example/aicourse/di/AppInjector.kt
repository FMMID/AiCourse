package com.example.aicourse.di

import android.app.Application
import android.content.Context
import com.example.aicourse.data.chat.local.ChatLocalDataSource.Companion.MAIN_CHAT_ID
import com.example.aicourse.data.chat.local.InMemoryChatDataSource
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.data.tools.context.ContextRepositoryImp
import com.example.aicourse.data.tools.context.SummarizeContextDataSource
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.tools.context.ContextRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO Заменить на Dependency Injection (Hilt, Koin, и т.д.)
object AppInjector {

    val existDataSources: MutableMap<ApiImplementation, BaseChatRemoteDataSource> = mutableMapOf()
    var inMemoryChatDataBase: InMemoryChatDataSource? = null
    var chatStateModel: ChatStateModel? = null
    var chatStrategy: ChatStrategy? = null

    fun createChatStateModel(): ChatStateModel = runBlocking {
        inMemoryChatDataBase = inMemoryChatDataBase ?: run { InMemoryChatDataSource() }
        return@runBlocking chatStateModel ?: run { inMemoryChatDataBase!!.getChatState(MAIN_CHAT_ID) }
    }

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

        val repository = ChatRepositoryImpl(context, remoteDataSource, InMemoryChatDataSource())

        if (chatStateModel == null) {
            GlobalScope.launch {
                chatStateModel = repository.getChatState(MAIN_CHAT_ID).getOrNull()
            }
        }

        return repository
    }

    fun createSendMessageChatUseCase(application: Application): SendMessageChatUseCase {
        val chatStateModel = createChatStateModel()
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)
        val simpleDataForSendStrategyImp = chatStrategy ?: run {
            SimpleChatStrategy(initChatStateModel = chatStateModel)
        }.also { chatStrategy = it }


        return SendMessageChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
    }

    fun createClearHistoryChatUseCase(application: Application): ClearHistoryChatUseCase {
        val chatStateModel = createChatStateModel()
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)
        val simpleDataForSendStrategyImp = chatStrategy ?: run {
            SimpleChatStrategy(initChatStateModel = chatStateModel)
        }.also { chatStrategy = it }

        return ClearHistoryChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
    }

    fun createGetHistoryChatUseCase(application: Application): GetHistoryChatUseCase {
        val chatStateModel = createChatStateModel()
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)

        return GetHistoryChatUseCase(chatRepository = repository)
    }
}