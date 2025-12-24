package com.example.aicourse.domain.chat.model

enum class RagMode {
    DISABLED,       // RAG выключен
    STANDARD,       // Обычный RAG (только Vector Search)
    WITH_RERANKER   // Advanced RAG (Vector Search + Reranker)
}