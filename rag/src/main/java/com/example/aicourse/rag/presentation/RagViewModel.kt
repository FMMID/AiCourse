package com.example.aicourse.rag.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.rag.data.local.JsonVectorStore
import com.example.aicourse.rag.domain.EmbeddingModel
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.domain.Reranker
import com.example.aicourse.rag.domain.model.DocumentChunk
import com.example.aicourse.rag.domain.textSplitter.RecursiveTextSplitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(
    application: Application,
    private val ragRepository: RagRepository,
    private val embeddingService: EmbeddingModel,
    private val reranker: Reranker
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState = _uiState.asStateFlow()

    private var activePipeline: RagPipeline? = null
    private var cachedFullChunks: List<DocumentChunk> = emptyList()

    init {
        loadIndicesList()
    }

    fun onIndexSelected(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Загружаем данные для отображения списка (через репозиторий)
            val chunks = ragRepository.loadIndex(name)
            cachedFullChunks = chunks

            // 2. Инициализируем Pipeline для поиска
            // Создаем Store, указывая путь к выбранному файлу
            val vectorStore = JsonVectorStore(getApplication(), "rag_indices/$name.json")

            // ВАЖНО: Загружаем индекс в память VectorStore, чтобы поиск работал
            vectorStore.loadIndex()

            activePipeline = RagPipeline(
                embeddingModel = embeddingService,
                vectorStore = vectorStore,
                textSplitter = RecursiveTextSplitter(),
                rerankerService = reranker
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedIndexName = name,
                processedChunks = chunks
            )
        }
    }

    fun onSearchQuery(query: String) {
        if (query.isBlank()) {
            // Если пусто — показываем все чанки (из кэша)
            _uiState.value = _uiState.value.copy(processedChunks = cachedFullChunks)
            return
        }

        val pipeline = activePipeline ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // ИСПОЛЬЗУЕМ ФУНКЦИЮ Retrieve!
                // Она сама сделает embed() и search()
                val relevantChunks = pipeline.retrieve(query) // Можно менять лимит

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    processedChunks = relevantChunks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка поиска: ${e.message}"
                )
            }
        }
    }

    fun onBackToList() {
        activePipeline = null
        cachedFullChunks = emptyList()
        _uiState.value = _uiState.value.copy(
            selectedIndexName = null,
            processedChunks = emptyList(),
            error = null
        )
        loadIndicesList()
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createNewIndex(indexName: String, fileUri: Uri) {
        viewModelScope.launch {
            hideCreateDialog()
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val content = readFileContent(fileUri)
                val sourceName = getFileName(fileUri)

                val relativePath = "rag_indices/$indexName.json"
                val vectorStore = JsonVectorStore(getApplication(), relativePath)

                val newPipeline = RagPipeline(
                    embeddingModel = embeddingService,
                    vectorStore = vectorStore,
                    textSplitter = RecursiveTextSplitter(),
                    reranker
                )

                val chunks = newPipeline.ingestDocument(relativePath, content)

                activePipeline = newPipeline
                cachedFullChunks = chunks

                loadIndicesList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedIndexName = indexName,
                    processedChunks = chunks
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    fun deleteIndex(indexName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ragRepository.deleteIndex(indexName)
            if (success) {
                if (_uiState.value.selectedIndexName == indexName) {
                    onBackToList()
                } else {
                    loadIndicesList()
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "Не удалось удалить $indexName")
            }
        }
    }

    fun onIndexLongClicked(indexName: String) {
        // Если уже выбран этот же элемент - снимаем выделение, иначе выделяем новый
        val currentTarget = _uiState.value.chatTargetId
        if (currentTarget == indexName) {
            clearChatSelection()
        } else {
            _uiState.value = _uiState.value.copy(chatTargetId = indexName)
        }
    }

    fun clearChatSelection() {
        _uiState.value = _uiState.value.copy(chatTargetId = null)
    }

    private fun loadIndicesList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                availableIndices = ragRepository.getAvailableIndices()
            )
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