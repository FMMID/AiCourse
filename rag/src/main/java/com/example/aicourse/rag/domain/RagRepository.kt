package com.example.aicourse.rag.domain

import com.example.aicourse.rag.domain.model.DocumentChunk
import java.io.File

interface RagRepository {

    suspend fun getAvailableIndices(): List<String>

    suspend fun loadIndex(name: String): List<DocumentChunk>

    suspend fun getIndexFile(name: String): File
}