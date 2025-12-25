package com.example.aicourse.rag.presentation

import com.example.aicourse.rag.domain.model.DocumentChunk

data class RagUiState(
    val availableIndices: List<String> = emptyList(),
    val selectedIndexName: String? = null,
    val processedChunks: List<DocumentChunk> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val selectedIndicesForChat: Set<String> = emptySet()
)