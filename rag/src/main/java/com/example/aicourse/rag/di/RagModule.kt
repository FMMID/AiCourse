package com.example.aicourse.rag.di

import android.app.Application
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.data.local.JsonVectorStore
import com.example.aicourse.rag.data.remote.embeddingService.OllamaEmbeddingService
import com.example.aicourse.rag.data.remote.reranker.OllamaRerankerService
import com.example.aicourse.rag.domain.EmbeddingModel
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.domain.Reranker
import com.example.aicourse.rag.domain.VectorStore
import com.example.aicourse.rag.domain.textSplitter.RecursiveTextSplitter
import com.example.aicourse.rag.domain.textSplitter.TextSplitter
import com.example.aicourse.rag.presentation.RagViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val ragModule = module {
    single<RagRepository> { RagRepositoryImp(androidContext()) }
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

    factory<VectorStore> { (ragFileName: String?) ->
        JsonVectorStore(context = androidContext(), fileName = ragFileName ?: "rag_index.json")
    }

    factory<RagPipeline> { (indexId: String?) ->
        RagPipeline(
            embeddingModel = get<EmbeddingModel>(),
            vectorStore = get<VectorStore> { parametersOf(indexId) },
            textSplitter = get<TextSplitter>(),
            rerankerService = get<Reranker>(),
        )
    }

    viewModel {
        RagViewModel(
            application = androidContext() as Application,
            ragRepository = get<RagRepository>(),
            embeddingService = get<EmbeddingModel>(),
            reranker = get<Reranker>()
        )
    }
}