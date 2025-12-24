package com.example.aicourse.rag.domain

import android.util.Log
import com.example.aicourse.rag.domain.model.DocumentChunk
import com.example.aicourse.rag.domain.textSplitter.TextSplitter
import java.util.UUID

class RagPipeline(
    private val embeddingModel: EmbeddingModel,
    private val vectorStore: VectorStore,
    private val textSplitter: TextSplitter,
    private val rerankerService: Reranker
) {

    // 1. Загрузка и Индексация
    suspend fun ingestDocument(fileName: String, content: String): List<DocumentChunk> {
        // Шаг 1: Разбивка на чанки
        val rawChunks = textSplitter.split(
            content,
            chunkSize = 300,
            overlap = 20
        )

        // Шаг 2: Генерация эмбеддингов
        val embeddings = embeddingModel.embedBatch(rawChunks)

        // Шаг 3: Создание документов
        val docs = rawChunks.mapIndexed { index, text ->
            DocumentChunk(
                id = UUID.randomUUID().toString(),
                text = text,
                source = fileName,
                embedding = embeddings[index]
            )
        }

        // Шаг 4: Сохранение в индекс
        vectorStore.addDocuments(docs)
        vectorStore.saveIndex()

        return docs
    }

    // Поиск (для использования в чате)
    suspend fun retrieve(
        query: String,
        limit: Int = 3,
        useReranker: Boolean = false
    ): List<DocumentChunk> {
        val queryVector = embeddingModel.embed(query)

        // ЭТАП 1: Получаем "широкую" выборку кандидатов через векторный поиск.
        // Берем в 3-5 раз больше, чем нужно в итоге, чтобы было из чего выбирать.
        val candidatesCount = if (useReranker) limit * 5 else limit

        // Используем низкий порог для поиска (0.2), чтобы не потерять потенциально полезное
        val candidates = vectorStore.search(
            queryEmbedding = queryVector,
            limit = candidatesCount,
            minScore = 0.2f
        )

        Log.d("RagPipeline", "Stage 1 (Vector): Found ${candidates.size} candidates")

        // Если реранкер не нужен или не подключен, возвращаем как есть
        if (!useReranker) {
            return candidates.take(limit)
        }

        // ЭТАП 2: Умная фильтрация через Ollama
        // Ставим порог 0.6 - только достаточно уверенные ответы
        val rankedDocs = rerankerService.rerank(
            query = query,
            documents = candidates,
            minScore = 0.4f
        )

        Log.d("RagPipeline", "Stage 2 (Reranker): Kept ${rankedDocs.size} documents")
        return rankedDocs.take(limit)
    }
}