package com.example.aicourse.rag.data.remote.reranker

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateResponse(
    val response: String
)