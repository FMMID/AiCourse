package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk

interface Reranker {

    /**
     * Принимает список кандидатов и возвращает только релевантные, отсортированные по качеству.
     */
    suspend fun rerank(
        query: String,
        documents: List<DocumentChunk>,
        minScore: Float = 0.5f // Порог отсечения (0.0 - 1.0)
    ): List<DocumentChunk>
}