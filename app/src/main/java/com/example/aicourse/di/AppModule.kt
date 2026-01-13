package com.example.aicourse.di

import com.example.aicourse.mcpclient.di.mcpModuleBase
import org.koin.dsl.module

/**
 * Основной модуль приложения
 * Агрегирует все подмодули DI
 *
 * Было: 174 строки монолитного кода
 * Стало: 15 строк - чистый агрегатор
 *
 * Подмодули:
 * - databaseModule: Room, DAO, Local Data Sources
 * - mcpModule: MCP clients и конфигурации
 * - networkModule: Remote Data Sources
 * - toolsModule: Tools для SimpleChatStrategy
 * - repositoryModule: Repositories
 * - useCaseModule: Use Cases (с исправлением бага #1)
 * - strategyModule: Chat strategies (с изоляцией runBlocking - баг #2)
 * - viewModelModule: ViewModels
 */
val appModule = module {
    includes(
        databaseModule,
        mcpModule,
        mcpModuleBase,
        networkModule,
        toolsModule,
        repositoryModule,
        useCaseModule,
        strategyModule,
        viewModelModule
    )
}
