package com.example.aicourse.rag.data.remote.reranker

import android.util.Log
import com.example.aicourse.rag.domain.Reranker
import com.example.aicourse.rag.domain.model.DocumentChunk
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OllamaRerankerService(
    private val baseUrl: String,
    private val modelName: String
) : Reranker {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000 // Реранкинг может занять время
            connectTimeoutMillis = 60_000
        }
    }

    override suspend fun rerank(
        query: String,
        documents: List<DocumentChunk>,
        minScore: Float
    ): List<DocumentChunk> {
        Log.d("OllamaReranker", "Starting rerank for ${documents.size} docs")

        // В реальном проде это делают батчами или параллельно (async/await),
        // но для простоты сделаем последовательно, чтобы не положить локальную Ollama.
        val scoredDocs = documents.map { doc ->
            val score = evaluateRelevance(query, doc.text)
            Log.d("OllamaReranker", "Doc score: $score | Text: ${doc.text.take(30)}...")
            doc.copy(score = score) // Предполагаем, что у DocumentChunk есть поле score, или можно добавить
        }

        // Фильтруем и сортируем
        return scoredDocs
            .filter { (it.score ?: 0f) >= minScore }
            .sortedByDescending { it.score }
    }

    private suspend fun evaluateRelevance(query: String, text: String): Float {
        // Промпт заставляет модель вывести ТОЛЬКО число.
        val prompt = """
            You are a relevance classifier. 
            Query: "$query"
            Context: "$text"
            
            Rate the relevance of the Context to the Query on a scale from 0.0 (irrelevant) to 1.0 (highly relevant).
            IMPORTANT: Output ONLY the number, no other text.
        """.trimIndent()

        return try {
            val response: OllamaGenerateResponse = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(OllamaGenerateRequest(model = modelName, prompt = prompt))
            }.body()

            // Парсим число из ответа модели
            response.response.trim().toFloatOrNull() ?: 0.0f
        } catch (e: Exception) {
            Log.e("OllamaReranker", "Error evaluating relevance: ${e.message}")
            0.0f
        }
    }
}