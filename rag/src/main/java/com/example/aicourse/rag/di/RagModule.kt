package com.example.aicourse.rag.di

import android.app.Application
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.data.remote.embeddingService.OllamaEmbeddingService
import com.example.aicourse.rag.data.remote.reranker.OllamaRerankerService
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
    // Репозиторий данных (файловая система)
    single<RagRepository> { RagRepositoryImp(androidContext()) }

    // Движок поиска (чистая математика)
    single { VectorSearchEngine() }

    // Внешние сервисы
    single<EmbeddingModel> {
        OllamaEmbeddingService(
            baseUrl = "http://10.0.2.2:11434",
            modelName = "nomic-embed-text:latest"
        )
    }
    single<Reranker> {
        OllamaRerankerService(
            baseUrl = "http://10.0.2.2:11434",
            modelName = "qwen2.5:latest"
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
            ragRepository = get<RagRepository>(),
            ragPipeline = get<RagPipeline>() // Инжектим сразу готовый Pipeline
        )
    }
}