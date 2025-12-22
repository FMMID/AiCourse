package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk
import com.example.aicourse.rag.domain.textSplitter.SimpleTextSplitter
import com.example.aicourse.rag.domain.textSplitter.TextSplitter
import java.util.UUID

class RagPipeline(
    private val embeddingModel: EmbeddingModel,
    private val vectorStore: VectorStore,
    private val textSplitter: TextSplitter = SimpleTextSplitter()
) {

    // 1. Загрузка и Индексация
    suspend fun ingestDocument(fileName: String, content: String): List<DocumentChunk> {
        // Шаг 1: Разбивка на чанки
        val rawChunks = textSplitter.split(content)

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
    suspend fun retrieve(query: String): List<DocumentChunk> {
        val queryVector = embeddingModel.embed(query)
        return vectorStore.search(queryVector)
    }
}