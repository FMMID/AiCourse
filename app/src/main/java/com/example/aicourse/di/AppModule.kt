package com.example.aicourse.di

import android.app.Application
import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.local.ChatLocalDataSource.Companion.MAIN_CHAT_ID
import com.example.aicourse.data.chat.local.RoomChatLocalDataSource
import com.example.aicourse.data.chat.local.room.ChatDatabase
import com.example.aicourse.data.chat.local.room.dao.ChatDao
import com.example.aicourse.data.chat.local.room.mapper.ChatStateMapper
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.data.settings.remote.McpRemoteDataSource
import com.example.aicourse.data.settings.repository.McpRepositoryImp
import com.example.aicourse.data.tools.context.ContextRepositoryImp
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.promt.adbManager.AdbManagerPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.domain.settings.usecase.GetLocalMcpToolsUseCase
import com.example.aicourse.domain.settings.usecase.SettingsChatUseCase
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.mcpclient.McpClientFactory
import com.example.aicourse.mcpclient.UserSession
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.settings.mvi.SettingsViewModel
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.RagRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module


private val initActiveUserPrompt = AdbManagerPrompt()
private val mcpConfigs = listOf<McpClientConfig>(
//    McpClientConfig(BuildConfig.MCP_NOTE_URL),
//    McpClientConfig(BuildConfig.MCP_NOTIFICATION_URL)
)
private val mcpClients = mcpConfigs.map { McpClientFactory.createMcpClient(it) }

val appModule = module {
    // --- Database & Local Data Source ---
    single<ChatDatabase> { ChatDatabase.getInstance(androidContext()) }
    single<ChatDao> { get<ChatDatabase>().chatDao() }
    factory<ChatStateMapper> { ChatStateMapper() }
    single<ChatLocalDataSource> {
        RoomChatLocalDataSource(
            chatDao = get<ChatDao>(),
            mapper = get<ChatStateMapper>(),
            initActiveUserPrompt = initActiveUserPrompt
        )
    }

    // MCP
    single<McpRemoteDataSource> { McpRemoteDataSource() }
    single<McpRepository> { McpRepositoryImp(mcpRemoteDataSource = get<McpRemoteDataSource>()) }


    // --- Remote Data Sources Factory ---
    factory<BaseChatRemoteDataSource> { (apiImplementation: ApiImplementation) ->
        when (apiImplementation) {
            ApiImplementation.GIGA_CHAT -> GigaChatDataSource(
                authorizationKey = apiImplementation.key,
                mcpClients = mcpClients,
                userId = UserSession.CURRENT_USER_ID
            )

            ApiImplementation.HUGGING_FACE -> HuggingFaceDataSource(
                apiToken = apiImplementation.key,
            )
        }
    }

    single<ChatStateModel> { runBlocking { get<ChatLocalDataSource>().getChatState(MAIN_CHAT_ID) } }

    // ContextRepository (всегда использует HuggingFace, как в AppInjector)
    single<ContextRepository> {
        val huggingFaceSource = get<BaseChatRemoteDataSource> { parametersOf(ApiImplementation.HUGGING_FACE) }
        ContextRepositoryImp(summarizeContextDataSource = huggingFaceSource)
    }

    // ChatRepository
    single<ChatRepository> {
        val stateModel = get<ChatStateModel>()
        val apiImpl = stateModel.settingsChatModel.currentUseApiImplementation
        val remoteDataSource = get<BaseChatRemoteDataSource> { parametersOf(apiImpl) }

        ChatRepositoryImpl(androidContext(), remoteDataSource, get())
    }

    // --- Strategies ---
    single<ChatStrategy> {
        SimpleChatStrategy(
            initChatStateModel = get<ChatStateModel>(),
            applicationContext = androidContext(),
            contextRepository = get<ContextRepository>(),
            initialSystemPrompt = initActiveUserPrompt,
            ragPipelineFactory = { indexId ->
                get<RagPipeline> { parametersOf(indexId) }
            }
        )
    }

    // --- Use Cases ---
    // UseCases обычно легкие и не хранят состояние, поэтому factory
    factory { SendMessageChatUseCase(chatRepository = get<ChatRepository>(), chatStrategy = get<ChatStrategy>()) }

    factory { ClearHistoryChatUseCase(chatRepository = get<ChatRepository>(), chatStrategy = get<ChatStrategy>()) }

    factory { GetHistoryChatUseCase(chatRepository = get<ChatRepository>()) }

    // --- ViewModels ---
    // SettingsViewModel
    viewModel {
        SettingsViewModel(
            application = androidContext() as Application,
            settingsChatUseCase = get<SettingsChatUseCase>(),
            getLocalMcpToolsUseCase = get<GetLocalMcpToolsUseCase>()
        )
    }

    // ChatViewModel
    // Принимает динамические параметры (chatId, ragIndexId) + UseCases
    viewModel { (chatId: String, ragIndexId: String?) ->
        ChatViewModel(
            application = androidContext() as Application,
            chatId = chatId,
            ragIndexId = ragIndexId,
            ragRepository = get<RagRepository>(),
            sendMessageChatUseCase = get<SendMessageChatUseCase>(),
            clearHistoryChatUseCase = get<ClearHistoryChatUseCase>(),
            getHistoryChatUseCase = get<GetHistoryChatUseCase>()
        )
    }
}