package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk

interface VectorStore {
    suspend fun addDocuments(documents: List<DocumentChunk>)

    suspend fun search(queryEmbedding: List<Float>, limit: Int = 3): List<DocumentChunk>

    suspend fun saveIndex()

    suspend fun loadIndex()
}
