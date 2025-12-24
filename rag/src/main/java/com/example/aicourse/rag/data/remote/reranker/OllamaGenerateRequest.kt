package com.example.aicourse.rag.data.remote.reranker

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val options: OllamaOptions = OllamaOptions(temperature = 0.7)
)

@Serializable
data class OllamaOptions(
    val temperature: Double,
    val num_ctx: Int = 4096
)