package com.example.aicourse.rag.data.local

import android.content.Context
import com.example.aicourse.rag.domain.VectorStore
import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.sqrt

class JsonVectorStore(
    private val context: Context,
    private val fileName: String = "rag_index.json"
) : VectorStore {

    private val memoryIndex = mutableListOf<DocumentChunk>()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override suspend fun addDocuments(documents: List<DocumentChunk>) {
        memoryIndex.addAll(documents)
    }

    override suspend fun saveIndex() {
        val jsonString = json.encodeToString(memoryIndex)
        val file = File(context.filesDir, fileName)
        file.writeText(jsonString)
    }

    override suspend fun loadIndex() {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val content = file.readText()
            memoryIndex.clear()
            memoryIndex.addAll(json.decodeFromString<List<DocumentChunk>>(content))
        }
    }

    override suspend fun search(queryEmbedding: List<Float>, limit: Int): List<DocumentChunk> {
        return memoryIndex
            .map { doc ->
                val similarity = cosineSimilarity(queryEmbedding, doc.embedding ?: emptyList())
                doc to similarity
            }
            .sortedByDescending { it.second } // Сортируем: от 1.0 (похож) к -1.0 (не похож)
            .take(limit)
            .map { (doc, score) ->
                doc.copy(score = score.toFloat())
            }
    }

    // Математика сравнения векторов
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