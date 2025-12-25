package com.example.aicourse.rag.domain

import android.util.Log
import com.example.aicourse.rag.domain.model.DocumentChunk
import com.example.aicourse.rag.domain.textSplitter.TextSplitter
import java.util.UUID

class RagPipeline(
    private val embeddingModel: EmbeddingModel,
    private val repository: RagRepository,
    private val searchEngine: VectorSearchEngine,
    private val textSplitter: TextSplitter,
    private val rerankerService: Reranker
) {

    // Храним активную базу знаний в оперативной памяти для быстрого поиска
    private var activeKnowledgeBase: List<DocumentChunk> = emptyList()

    /**
     * Загружает выбранные источники в память для поиска
     */
    suspend fun loadActiveContext(sourceNames: List<String>) {
        activeKnowledgeBase = repository.loadIndices(sourceNames)
        Log.d("RagPipeline", "Loaded ${activeKnowledgeBase.size} chunks from sources: $sourceNames")
    }

    /**
     * Создание нового индекса из текста и сохранение через репозиторий
     */
    suspend fun ingestDocument(fileName: String, content: String): List<DocumentChunk> {
        // 1. Разбивка
        val rawChunks = textSplitter.split(content, chunkSize = 300, overlap = 20)

        // 2. Эмбеддинги
        val embeddings = embeddingModel.embedBatch(rawChunks)

        // 3. Создание объектов (source = имя файла)
        val docs = rawChunks.mapIndexed { index, text ->
            DocumentChunk(
                id = UUID.randomUUID().toString(),
                text = text,
                source = fileName,
                embedding = embeddings[index]
            )
        }

        // 4. Сохранение через репозиторий (заменяем старый файл, если был)
        repository.saveIndex(fileName, docs)

        return docs
    }

    // Поиск (для использования в чате)
    suspend fun retrieve(
        query: String,
        limit: Int = 3,
        useReranker: Boolean = false
    ): List<DocumentChunk> {
        if (activeKnowledgeBase.isEmpty()) {
            Log.w("RagPipeline", "Knowledge base is empty. Did you call loadActiveContext?")
            return emptyList()
        }

        val queryVector = embeddingModel.embed(query)

        // ЭТАП 1: Векторный поиск через SearchEngine
        val candidatesCount = if (useReranker) limit * 5 else limit

        val candidates = searchEngine.search(
            queryEmbedding = queryVector,
            documents = activeKnowledgeBase, // Ищем по загруженным в память чанкам
            limit = candidatesCount,
            minScore = 0.2f
        )

        Log.d("RagPipeline", "Stage 1 (Vector): Found ${candidates.size} candidates")

        if (!useReranker) return candidates.take(limit)

        // ЭТАП 2: Reranker
        val rankedDocs = rerankerService.rerank(
            query = query,
            documents = candidates,
            minScore = 0.4f // Повысил порог для качества
        )

        Log.d("RagPipeline", "Stage 2 (Reranker): Kept ${rankedDocs.size} documents")
        return rankedDocs.take(limit)
    }
}