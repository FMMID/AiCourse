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
    private val rerankerService: Reranker,
    private val queryExpander: QueryExpander = SimpleQueryExpander()
) {

    // Храним активную базу знаний в оперативной памяти для быстрого поиска
    var activeKnowledgeBase: List<DocumentChunk> = emptyList()
        private set

    suspend fun getAvailableIndices(): List<String> {
        return repository.getAvailableIndices()
    }

    suspend fun deleteIndex(indexName: String): Boolean {
        // 1. Удаляем физически
        val isDeleted = repository.deleteIndex(indexName)

        // 2. Если удаление успешно, чистим активную память, чтобы не искать по удаленному
        if (isDeleted) {
            val oldSize = activeKnowledgeBase.size
            // Удаляем чанки, у которых source совпадает с удаляемым индексом
            // (Предполагаем, что source == indexName или имя файла)
            activeKnowledgeBase = activeKnowledgeBase.filter { it.source != indexName && it.source != "$indexName.json" }

            Log.d("RagPipeline", "Deleted index '$indexName'. Memory cleaned: $oldSize -> ${activeKnowledgeBase.size} chunks")
        }
        return isDeleted
    }

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
        val rawChunks = textSplitter.split(content, chunkSize = 1000, overlap = 150)
        val embeddings = embeddingModel.embedBatch(rawChunks)

        val docs = rawChunks.mapIndexed { index, text ->
            DocumentChunk(
                id = UUID.randomUUID().toString(),
                text = text,
                source = fileName, // Важно сохранять имя файла как source
                embedding = embeddings[index]
            )
        }

        // Сохраняем через репозиторий
        repository.saveIndex(fileName, docs)
        return docs
    }

    // Поиск (для использования в чате)
    suspend fun retrieve(
        query: String,
        limit: Int = 3,
        useReranker: Boolean = false,
        useMultiQuery: Boolean = false
    ): List<DocumentChunk> {
        if (activeKnowledgeBase.isEmpty()) {
            Log.w("RagPipeline", "Knowledge base is empty. Did you call loadActiveContext?")
            return emptyList()
        }

        // ЭТАП 0 (опционально): Мульти-запросы
        val queries = if (useMultiQuery) {
            queryExpander.expandQuery(query)
        } else {
            listOf(query)
        }

        Log.d("RagPipeline", "Searching with ${queries.size} query variations")

        // ЭТАП 1: Векторный поиск через SearchEngine
        val candidatesCount = if (useReranker) limit * 15 else limit

        // Ищем по каждой вариации запроса
        val allCandidates = queries.flatMap { queryVariant ->
            val queryVector = embeddingModel.embed(queryVariant)
            searchEngine.search(
                queryEmbedding = queryVector,
                documents = activeKnowledgeBase,
                limit = candidatesCount,
                minScore = 0.45f
            )
        }

        // Объединяем результаты, убираем дубликаты по ID, сохраняем максимальный score
        val candidates = allCandidates
            .groupBy { it.id }
            .map { (_, chunks) ->
                chunks.maxByOrNull { it.score ?: 0f }!!
            }
            .sortedByDescending { it.score }
            .take(candidatesCount)

        candidates.forEach { doc ->
            Log.d("RAG", "Score: ${doc.score}, Text: ${doc.text.take(50)}...")
        }

        Log.d("RAG", "Top score: ${candidates.firstOrNull()?.score}, Worst: ${candidates.lastOrNull()?.score}")

        Log.d("RagPipeline", "Stage 1 (Vector): Found ${candidates.size} candidates")

        if (!useReranker) return candidates.take(limit)

        // ЭТАП 2: Reranker
        val rankedDocs = rerankerService.rerank(
            query = query,
            documents = candidates,
            minScore = 0.2f // Низкий порог для пропуска частично релевантных чанков
        )

        Log.d("RagPipeline", "Stage 2 (Reranker): Kept ${rankedDocs.size} documents")
        return rankedDocs.take(limit)
    }
}