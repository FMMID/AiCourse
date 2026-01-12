package com.example.aicourse.rag.di

import android.app.Application
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.data.remote.embeddingService.OllamaEmbeddingService
import com.example.aicourse.rag.data.remote.reranker.OllamaRerankerService
import com.example.aicourse.rag.di.config.OllamaConfig
import com.example.aicourse.rag.domain.EmbeddingModel
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.domain.Reranker
import com.example.aicourse.rag.domain.VectorSearchEngine
import com.example.aicourse.rag.domain.textSplitter.RecursiveTextSplitter
import com.example.aicourse.rag.domain.textSplitter.TextSplitter
import com.example.aicourse.rag.presentation.RagViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ragModule = module {
    // Ollama Configuration (убирает хардкод)
    single { OllamaConfig() }

    // Репозиторий данных (файловая система)
    single<RagRepository> { RagRepositoryImp(androidContext()) }

    // Движок поиска (чистая математика)
    single { VectorSearchEngine() }

    // Внешние сервисы
    single<EmbeddingModel> {
        val config = get<OllamaConfig>()
        OllamaEmbeddingService(
            baseUrl = config.baseUrl,
            modelName = config.embeddingModelName
        )
    }
    single<Reranker> {
        val config = get<OllamaConfig>()
        OllamaRerankerService(
            baseUrl = config.baseUrl,
            modelName = config.rerankerModelName
        )
    }
    single<TextSplitter> { RecursiveTextSplitter() }

    // Pipeline теперь собирает всё вместе.
    // Мы делаем его factory, чтобы при желании можно было создавать несколько независимых,
    // но обычно достаточно одного на экран.
    factory {
        RagPipeline(
            embeddingModel = get<EmbeddingModel>(),
            repository = get<RagRepository>(),     // Новая зависимость
            searchEngine = get<VectorSearchEngine>(), // Новая зависимость
            textSplitter = get<TextSplitter>(),
            rerankerService = get<Reranker>(),
        )
    }

    viewModel {
        RagViewModel(
            application = androidContext() as Application,
            ragPipeline = get<RagPipeline>() // Инжектим сразу готовый Pipeline
        )
    }
}