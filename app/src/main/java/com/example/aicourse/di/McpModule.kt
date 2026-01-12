package com.example.aicourse.di

import com.example.aicourse.mcpclient.data.McpRemoteDataSource
import com.example.aicourse.di.config.ApiConfig
import com.example.aicourse.mcpclient.McpClientConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Модуль DI для MCP (Model Context Protocol) компонентов
 * - MCP Clients
 * - MCP Configurations
 * - MCP Remote Data Sources
 */
val mcpModule = module {
    // API Configuration
    single { ApiConfig() }

    // MCP Client Configs
    single(named("mcpNoteConfig")) {
        McpClientConfig(get<ApiConfig>().mcpNoteUrl)
    }

    single(named("mcpNotificationConfig")) {
        McpClientConfig(get<ApiConfig>().mcpNotificationUrl)
    }

    // MCP Clients (закомментировано в текущем коде, оставляем так)
    // Было глобальными переменными в AppModule.kt:45-49
    // single<List<McpClient>>(named("mcpClients")) {
    //     listOf(
    //         McpClientFactory.createMcpClient(get(named("mcpNoteConfig"))),
    //         McpClientFactory.createMcpClient(get(named("mcpNotificationConfig")))
    //     )
    // }
}
