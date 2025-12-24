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
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
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
        val prompt = """
            Task: Evaluate if the provided Context contains the answer to the Query.
            
            Query: "$query"
            Context: "$text"
            
            Scoring Rules:
            1.0 - The Context contains the exact answer to the Query.
            0.5 - The Context discusses the topic but does not contain the answer.
            0.1 - The Context is irrelevant.
            
            IMPORTANT: Output ONLY the number (e.g., 1.0, 0.5, or 0.1). No explanation.
        """.trimIndent()

        return try {
            // 1. Получаем сырой ответ, а не парсим сразу
            val httpResponse: HttpResponse = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(OllamaGenerateRequest(model = modelName, prompt = prompt))
            }

            val responseBody = httpResponse.bodyAsText()

            // 2. Логируем, что реально ответила Ollama
            // Если там ошибка, мы увидим {"error": "..."}
            Log.d("OllamaReranker", "Raw API Response: $responseBody")

            // 3. Проверяем статус код
            if (httpResponse.status.value !in 200..299) {
                Log.e("OllamaReranker", "Server returned error: ${httpResponse.status}")
                return 0.0f
            }

            // 4. Парсим вручную
            val parsedResponse = json.decodeFromString<OllamaGenerateResponse>(responseBody)
            parsedResponse.response.trim().toFloatOrNull() ?: 0.0f

        } catch (e: Exception) {
            Log.e("OllamaReranker", "Error evaluating relevance: ${e.message}")
            0.0f
        }
    }
}