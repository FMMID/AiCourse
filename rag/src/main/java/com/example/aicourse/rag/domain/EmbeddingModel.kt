package com.example.aicourse.rag.domain

interface EmbeddingModel {
    suspend fun embed(text: String): List<Float>

    suspend fun embedBatch(texts: List<String>): List<List<Float>>
}
