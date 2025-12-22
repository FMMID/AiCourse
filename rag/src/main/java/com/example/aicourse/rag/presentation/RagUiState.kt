package com.example.aicourse.rag.presentation

import com.example.aicourse.rag.domain.model.DocumentChunk

data class RagUiState(
    val isLoading: Boolean = false,
    val processedChunks: List<DocumentChunk> = emptyList(),
    val error: String? = null
)