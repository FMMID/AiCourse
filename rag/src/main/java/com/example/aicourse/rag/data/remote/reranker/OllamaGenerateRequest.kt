package com.example.aicourse.rag.data.remote.reranker

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val options: Map<String, String> = mapOf("temperature" to "0.0") // Температура 0 для стабильности
)