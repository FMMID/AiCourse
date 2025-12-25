package com.example.aicourse.rag.data

import android.content.Context
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlinx.serialization.encodeToString
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

    override suspend fun deleteIndex(indexName: String): Boolean {
        val file = File(indicesDir, "$indexName.json")
        return if (file.exists()) file.delete() else false
    }

    override suspend fun saveIndex(
        name: String,
        chunks: List<DocumentChunk>
    ) {
        val safeName = name.replace(".json", "")
        val file = File(indicesDir, "$safeName.json")
        val jsonString = json.encodeToString(chunks)
        file.writeText(jsonString)
    }

    override suspend fun loadIndices(names: List<String>): List<DocumentChunk> {
        val combinedChunks = mutableListOf<DocumentChunk>()

        names.forEach { name ->
            val file = File(indicesDir, "$name.json")
            if (file.exists()) {
                try {
                    val chunks = json.decodeFromString<List<DocumentChunk>>(file.readText())
                    combinedChunks.addAll(chunks)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return combinedChunks
    }
}
