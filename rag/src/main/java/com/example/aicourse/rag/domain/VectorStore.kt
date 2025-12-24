package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk

interface VectorStore {
    suspend fun addDocuments(documents: List<DocumentChunk>)

    suspend fun search(
        queryEmbedding: List<Float>,
        limit: Int = 3,
        minScore: Float = 0.5f
    ): List<DocumentChunk>

    suspend fun saveIndex()

    suspend fun loadIndex()
}
