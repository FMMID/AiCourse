package com.example.aicourse.rag.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DocumentChunk(
    val id: String,
    val text: String,
    val source: String,
    val embedding: List<Float>? = null // Вектор. Null, если еще не сгенерирован
)