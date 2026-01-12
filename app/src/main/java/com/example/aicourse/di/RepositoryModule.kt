package com.example.aicourse.di

import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.repository.ChatRepositoryImpl
import com.example.aicourse.mcpclient.data.McpRepositoryImp
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.mcpclient.domain.McpRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * Модуль DI для репозиториев
 * - ChatRepository
 * - McpRepository
 */
val repositoryModule = module {

    // Chat Repository (требует ragIds для получения ChatStateModel)
    factory<ChatRepository> { (ragIds: List<String>) ->
        val stateModel = get<ChatStateModel> { parametersOf(ragIds) }
        val apiImpl = stateModel.settingsChatModel.currentUseApiImplementation

        ChatRepositoryImpl(
            context = androidContext(),
            remoteDataSource = get<BaseChatRemoteDataSource> { parametersOf(apiImpl) },
            localDataSource = get()
        )
    }
}
