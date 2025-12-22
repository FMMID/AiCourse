package com.example.aicourse.rag.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DocumentChunk(
    val id: String,
    val text: String,
    val source: String,
    val embedding: List<Float>? = null,
    @Transient
    val score: Float? = null
)