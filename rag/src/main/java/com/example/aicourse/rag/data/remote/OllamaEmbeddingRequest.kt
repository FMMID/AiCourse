package com.example.aicourse.rag.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class OllamaEmbeddingRequest(
    val model: String,
    val prompt: String
)
