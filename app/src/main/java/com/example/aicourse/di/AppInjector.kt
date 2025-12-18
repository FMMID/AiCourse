package com.example.aicourse.di

import android.app.Application
import android.content.Context
import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.local.ChatLocalDataSource.Companion.MAIN_CHAT_ID
import com.example.aicourse.data.chat.local.RoomChatLocalDataSource
import com.example.aicourse.data.chat.local.room.ChatDatabase
import com.example.aicourse.data.chat.local.room.mapper.ChatStateMapper
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.data.settings.remote.McpRemoteDataSource
import com.example.aicourse.data.settings.repository.McpRepositoryImp
import com.example.aicourse.data.tools.context.ContextRepositoryImp
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.mcpclient.McpClientFactory
import com.example.aicourse.mcpclient.McpClientType
import com.example.aicourse.mcpclient.UserSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO Заменить на Dependency Injection (Hilt, Koin, и т.д.)
object AppInjector {

    // TODO пока хардкодим
    val mcpClients = listOf(
        McpClientFactory.createMcpClient(McpClientType.NOTE_SERVICE),
        McpClientFactory.createMcpClient(McpClientType.SEND_INFORMATION_SERVICE)

    )
    val existDataSources: MutableMap<ApiImplementation, BaseChatRemoteDataSource> = mutableMapOf()
    var mcpRemoteDatabase: McpRemoteDataSource? = null
    var chatLocalDataSource: ChatLocalDataSource? = null
    var chatStateModel: ChatStateModel? = null
    var chatStrategy: ChatStrategy? = null

    /**
     * Создаёт или возвращает существующий ChatLocalDataSource
     * Использует Room Database для персистентного хранения
     */
    private fun getChatLocalDataSource(context: Context): ChatLocalDataSource {
        return chatLocalDataSource ?: run {
            val database = ChatDatabase.getInstance(context)
            val mapper = ChatStateMapper()
            RoomChatLocalDataSource(database.chatDao(), mapper)
        }.also { chatLocalDataSource = it }
    }

    fun createChatStateModel(context: Context): ChatStateModel = runBlocking {
        val localDataSource = getChatLocalDataSource(context)
        return@runBlocking chatStateModel ?: run { localDataSource.getChatState(MAIN_CHAT_ID) }
    }

    fun createContextRepository(settingsChatModel: SettingsChatModel): ContextRepository {
        val summarizeContextDataSource = existDataSources.getOrPut(ApiImplementation.HUGGING_FACE) {
            HuggingFaceDataSource(ApiImplementation.HUGGING_FACE.key)
        }

        return ContextRepositoryImp(summarizeContextDataSource)
    }

    fun createMcpRepository(): McpRepository {
        mcpRemoteDatabase = mcpRemoteDatabase ?: McpRemoteDataSource()
        val mcpRepository = McpRepositoryImp(mcpRemoteDatabase!!)
        return mcpRepository
    }

    fun createChatRepository(context: Context, settingsChatModel: SettingsChatModel): ChatRepository {
        val remoteDataSource = when (val apiImplementation = settingsChatModel.currentUseApiImplementation) {
            ApiImplementation.GIGA_CHAT -> {
                existDataSources.getOrPut(ApiImplementation.GIGA_CHAT) {
                    GigaChatDataSource(
                        authorizationKey = apiImplementation.key,
                        mcpClients = mcpClients,
                        userId = UserSession.CURRENT_USER_ID
                    )
                }
            }

            ApiImplementation.HUGGING_FACE -> {
                existDataSources.getOrPut(ApiImplementation.HUGGING_FACE) { HuggingFaceDataSource(apiImplementation.key) }
            }
        }

        val localDataSource = getChatLocalDataSource(context)
        val repository = ChatRepositoryImpl(context, remoteDataSource, localDataSource)

        if (chatStateModel == null) {
            GlobalScope.launch {
                chatStateModel = repository.getChatState(MAIN_CHAT_ID).getOrNull()
            }
        }

        return repository
    }

    fun createSendMessageChatUseCase(application: Application): SendMessageChatUseCase {
        val chatStateModel = createChatStateModel(application)
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)
        val simpleDataForSendStrategyImp = chatStrategy ?: run {
            SimpleChatStrategy(initChatStateModel = chatStateModel, applicationContext = application)
        }.also { chatStrategy = it }


        return SendMessageChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
    }

    fun createClearHistoryChatUseCase(application: Application): ClearHistoryChatUseCase {
        val chatStateModel = createChatStateModel(application)
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)
        val simpleDataForSendStrategyImp = chatStrategy ?: run {
            SimpleChatStrategy(initChatStateModel = chatStateModel, applicationContext = application)
        }.also { chatStrategy = it }

        return ClearHistoryChatUseCase(chatRepository = repository, chatStrategy = simpleDataForSendStrategyImp)
    }

    fun createGetHistoryChatUseCase(application: Application): GetHistoryChatUseCase {
        val chatStateModel = createChatStateModel(application)
        val repository = createChatRepository(application, chatStateModel.settingsChatModel)

        return GetHistoryChatUseCase(chatRepository = repository)
    }
}