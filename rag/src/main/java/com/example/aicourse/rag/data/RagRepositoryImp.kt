package com.example.aicourse.rag.data

import android.content.Context
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlinx.serialization.json.Json
import java.io.File

class RagRepositoryImp(context: Context) : RagRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val indicesDir = File(context.filesDir, "rag_indices")

    init {
        if (!indicesDir.exists()) indicesDir.mkdirs()
    }

    // 1. Получить список всех RAG-файлов
    override suspend fun getAvailableIndices(): List<String> {
        return indicesDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    // 2. Загрузить конкретный индекс
    override suspend fun loadIndex(name: String): List<DocumentChunk> {
        val file = File(indicesDir, "$name.json")
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<DocumentChunk>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 3. Получить путь к файлу для сохранения (используется в Pipeline)
    override suspend fun getIndexFile(name: String): File {
        return File(indicesDir, "$name.json")
    }
}
