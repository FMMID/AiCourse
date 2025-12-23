package com.example.aicourse.di

import android.app.Application
import com.example.aicourse.BuildConfig
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
import com.example.aicourse.domain.chat.promt.adbManager.AdbManagerPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.strategy.ChatStrategy
import com.example.aicourse.domain.chat.strategy.SimpleChatStrategy
import com.example.aicourse.domain.chat.usecase.ClearHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.GetHistoryChatUseCase
import com.example.aicourse.domain.chat.usecase.SendMessageChatUseCase
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.mcpclient.McpClientFactory
import com.example.aicourse.mcpclient.UserSession
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.settings.mvi.SettingsViewModel
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.presentation.RagViewModel
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {
// --- Database & Local Data Source ---
    single { ChatDatabase.getInstance(androidContext()) }

    single { get<ChatDatabase>().chatDao() }

    factory { ChatStateMapper() }

    single<ChatLocalDataSource> {
        RoomChatLocalDataSource(get(), get())
    }

    // --- MCP Configuration & Clients ---
    single {
        listOf(
            McpClientConfig(BuildConfig.MCP_NOTE_URL),
            McpClientConfig(BuildConfig.MCP_NOTIFICATION_URL)
        )
    }

    // Создаем список клиентов (single, как val mcpClients в AppInjector)
    single {
        get<List<McpClientConfig>>().map { McpClientFactory.createMcpClient(it) }
    }

    single { McpRemoteDataSource() }

    single<McpRepository> { McpRepositoryImp(get()) }

    // --- Initial Prompts ---
    single { AdbManagerPrompt() }

    // --- Chat State Model (Stateful) ---
    // В AppInjector это делается через runBlocking при первом обращении.
    // Оставляем single, чтобы загрузить один раз.
    single<ChatStateModel> {
        runBlocking {
            get<ChatLocalDataSource>().getChatState(MAIN_CHAT_ID)
        }
    }

    // --- Remote Data Sources Factory ---
    // Используем factory с параметрами, чтобы динамически выбирать реализацию и передавать ключ.
    // Вызывается как: get<BaseChatRemoteDataSource> { parametersOf(apiImplementation) }
    factory<BaseChatRemoteDataSource> { (apiImplementation: ApiImplementation) ->
        when (apiImplementation) {
            ApiImplementation.GIGA_CHAT -> GigaChatDataSource(
                authorizationKey = apiImplementation.key,
                mcpClients = get(), // Инжектим созданные выше клиенты
                userId = UserSession.CURRENT_USER_ID
            )

            ApiImplementation.HUGGING_FACE -> HuggingFaceDataSource(
                apiToken = apiImplementation.key,
            )
        }
    }

    // --- Repositories ---

    // ContextRepository (всегда использует HuggingFace, как в AppInjector)
    single<ContextRepository> {
        val huggingFaceSource = get<BaseChatRemoteDataSource> {
            parametersOf(ApiImplementation.HUGGING_FACE)
        }
        ContextRepositoryImp(huggingFaceSource)
    }

    // ChatRepository
    // Зависит от текущих настроек в ChatStateModel
    single<ChatRepository> {
        val stateModel = get<ChatStateModel>()
        val apiImpl = stateModel.settingsChatModel.currentUseApiImplementation

        // Получаем нужный RemoteDataSource на основе настроек
        val remoteDataSource = get<BaseChatRemoteDataSource> { parametersOf(apiImpl) }

        ChatRepositoryImpl(androidContext(), remoteDataSource, get())
    }

    // --- Strategies ---
    single<ChatStrategy> {
        SimpleChatStrategy(
            initChatStateModel = get(),
            applicationContext = androidContext()
        )
    }

    single<RagRepository> { RagRepositoryImp(androidContext()) }

    // --- Use Cases ---
    // UseCases обычно легкие и не хранят состояние, поэтому factory
    factory { SendMessageChatUseCase(chatRepository = get(), chatStrategy = get()) }

    factory { ClearHistoryChatUseCase(chatRepository = get(), chatStrategy = get()) }

    factory { GetHistoryChatUseCase(chatRepository = get()) }

    // --- ViewModels ---

    // SettingsViewModel
    viewModel {
        SettingsViewModel(
            application = androidContext() as Application
        )
    }

    // RagViewModel
    viewModel {
        RagViewModel(
            application = androidContext() as Application
        )
    }

    // ChatViewModel
    // Принимает динамические параметры (chatId, ragIndexId) + UseCases
    viewModel { (chatId: String, ragIndexId: String?) ->
        ChatViewModel(
            application = androidContext() as Application,
            chatId = chatId,
            ragIndexId = ragIndexId,
            // Внимание: RagRepository должен быть где-то объявлен (single/factory),
            // иначе здесь упадет. Если его нет, добавь `single { RagRepositoryImpl(...) }` выше.
            ragRepository = get(),
            sendMessageChatUseCase = get(),
            clearHistoryChatUseCase = get(),
            getHistoryChatUseCase = get()
        )
    }
}