package com.example.aicourse.rag.data.remote.embeddingService

import kotlinx.serialization.Serializable

@Serializable
data class OllamaEmbeddingResponse(
    val embedding: List<Float>
)