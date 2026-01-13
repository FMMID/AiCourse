package com.example.aicourse.mcpclient.di

import com.example.aicourse.mcpclient.data.McpRemoteDataSource
import com.example.aicourse.mcpclient.data.McpRepositoryImp
import com.example.aicourse.mcpclient.domain.McpRepository
import org.koin.dsl.module

val mcpModuleBase = module {

    single<McpRemoteDataSource> { McpRemoteDataSource() }

    single<McpRepository> { McpRepositoryImp(mcpRemoteDataSource = get()) }
}
