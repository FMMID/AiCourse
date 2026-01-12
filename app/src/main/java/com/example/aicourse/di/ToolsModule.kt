package com.example.aicourse.di

import com.example.aicourse.data.tools.context.ContextRepositoryImp
import com.example.aicourse.di.config.ContextConfig
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.domain.tools.context.ContextWindowManager
import com.example.aicourse.domain.tools.modelInfo.ModelInfoManager
import com.example.aicourse.domain.tools.tokenComparePrevious.TokenCompareManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Модуль DI для Tools
 * - ContextWindowManager
 * - TokenCompareManager
 * - ModelInfoManager
 * - ContextRepository
 *
 * Убирает ручное создание зависимостей из SimpleChatStrategy.kt:51-75
 */
val toolsModule = module {
    // Context Configuration
    single { ContextConfig() }

    // Context Repository (всегда использует HuggingFace для суммаризации)
    // Убирает хардкод из AppModule.kt:96-99
    single<ContextRepository> {
        ContextRepositoryImp(
            summarizeContextDataSource = get(named("huggingFace"))
        )
    }

    // Context Window Manager
    // Было создано вручную в SimpleChatStrategy.kt:51-60
    factory<ContextWindowManager> {
        ContextWindowManager(
            targetContextWindow = get<ContextConfig>().toContextWindow(),
            contextRepository = get(),
            applicationContext = androidContext()
        )
    }

    // Token Compare Manager
    // Было создано вручную в SimpleChatStrategy.kt:68
    factory<TokenCompareManager> {
        TokenCompareManager()
    }

    // Model Info Manager
    // Было создано вручную в SimpleChatStrategy.kt:64
    factory<ModelInfoManager> {
        ModelInfoManager()
    }
}
