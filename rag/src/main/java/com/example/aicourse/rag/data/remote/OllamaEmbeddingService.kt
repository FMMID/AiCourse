package com.example.aicourse.rag.data.remote

import android.util.Log
import com.example.aicourse.rag.domain.EmbeddingModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OllamaEmbeddingService(
    private val baseUrl: String,
    private val modelName: String
) : EmbeddingModel {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("OllamaClient", message)
                }
            }
        }
    }

    override suspend fun embed(text: String): List<Float> {
        return try {
            val response: OllamaEmbeddingResponse = client.post("$baseUrl/api/embeddings") {
                contentType(ContentType.Application.Json)
                setBody(OllamaEmbeddingRequest(model = modelName, prompt = text))
            }.body()

            response.embedding
        } catch (e: Exception) {
            Log.e("OllamaClient", "Error generating embedding: ${e.message}")
            emptyList()
        }
    }

    override suspend fun embedBatch(texts: List<String>): List<List<Float>> {
        // Ollama пока не поддерживает батчинг в /api/embeddings нативно (one call - one vector),
        // поэтому делаем последовательно или параллельно.
        // Для простоты здесь последовательно:
        return texts.map { embed(it) }
    }


}