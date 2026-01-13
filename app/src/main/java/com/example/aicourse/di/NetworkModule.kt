package com.example.aicourse.di

import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.gigachat.GigaChatDataSource
import com.example.aicourse.data.chat.remote.huggingface.HuggingFaceDataSource
import com.example.aicourse.data.tools.context.SummarizeContextDataSource
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.mcpclient.UserSession
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Модуль DI для сетевых компонентов
 * - Remote Data Sources
 * - API Clients
 */
val networkModule = module {
    // Remote Data Sources Factory
    factory<BaseChatRemoteDataSource> { (apiImplementation: ApiImplementation) ->
        when (apiImplementation) {
            ApiImplementation.GIGA_CHAT -> GigaChatDataSource(
                authorizationKey = apiImplementation.key,
                mcpClients = listOf(
                    get(named("mcpGitClient"))
                ), // Было: mcpClients (глобальная переменная)
                userId = UserSession.CURRENT_USER_ID
            )

            ApiImplementation.HUGGING_FACE -> HuggingFaceDataSource(
                apiToken = apiImplementation.key
            )
        }
    }

    // Named singleton для HuggingFace (используется в ContextRepository)
    // Убирает хардкод из AppModule.kt:96-99
    single<SummarizeContextDataSource>(named("huggingFace")) {
        get<BaseChatRemoteDataSource> { parametersOf(ApiImplementation.HUGGING_FACE) }
    }
}
