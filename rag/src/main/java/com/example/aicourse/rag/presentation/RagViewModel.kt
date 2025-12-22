package com.example.aicourse.rag.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.rag.data.local.JsonVectorStore
import com.example.aicourse.rag.data.remote.OllamaEmbeddingService
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.textSplitter.SimpleTextSplitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(application: Application) : AndroidViewModel(application) {

    private val pipeline = RagPipeline(
        embeddingModel = OllamaEmbeddingService(
            baseUrl = "http://10.0.2.2:11434",
            modelName = "nomic-embed-text:latest"
        ),
        vectorStore = JsonVectorStore(
            context = application,
            fileName = "text_rag_index.json"
        ),
        textSplitter = SimpleTextSplitter()
    )

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Читаем файл (в IO потоке)
                val fileContent = readFileContent(uri)
                val fileName = getFileName(uri)

                // 2. Запускаем Pipeline
                val chunks = pipeline.ingestDocument(fileName, fileContent)

                // 3. Обновляем UI успешным результатом
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    processedChunks = chunks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private suspend fun readFileContent(uri: Uri): String = withContext(Dispatchers.IO) {
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: throw Exception("Cannot open file")
    }

    private fun getFileName(uri: Uri): String {
        // Упрощенно возвращаем путь или заглушку.
        // В продакшене тут нужен запрос к ContentResolver для получения DISPLAY_NAME
        return uri.lastPathSegment ?: "unknown_file.txt"
    }
}