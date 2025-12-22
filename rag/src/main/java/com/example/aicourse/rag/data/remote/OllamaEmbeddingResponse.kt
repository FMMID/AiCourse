package com.example.aicourse.rag.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class OllamaEmbeddingResponse(
    val embedding: List<Float>
)