package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlin.math.sqrt

class VectorSearchEngine {

    /**
     * Выполняет поиск по переданному списку документов (documents)
     */
    fun search(
        queryEmbedding: List<Float>,
        documents: List<DocumentChunk>,
        limit: Int,
        minScore: Float
    ): List<DocumentChunk> {
        if (documents.isEmpty()) return emptyList()

        return documents
            .map { doc ->
                val similarity = cosineSimilarity(queryEmbedding, doc.embedding ?: emptyList())
                doc to similarity
            }
            .filter { it.second >= minScore }
            .sortedByDescending { it.second }
            .take(limit)
            .map { (doc, score) ->
                doc.copy(score = score.toFloat())
            }
    }

    private fun cosineSimilarity(v1: List<Float>, v2: List<Float>): Double {
        if (v1.size != v2.size || v1.isEmpty()) return 0.0

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }

        if (normA == 0.0 || normB == 0.0) return 0.0
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }
}