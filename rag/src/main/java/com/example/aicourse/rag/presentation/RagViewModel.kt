package com.example.aicourse.rag.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.data.local.JsonVectorStore
import com.example.aicourse.rag.data.remote.OllamaEmbeddingService
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(application: Application) : AndroidViewModel(application) {

    private val ragRepository = RagRepositoryImp(application)
    private val embeddingService = OllamaEmbeddingService(
        baseUrl = "http://10.0.2.2:11434",
        modelName = "nomic-embed-text:latest"
    )

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
                vectorStore = vectorStore
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
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 1. Читаем файл
                val content = readFileContent(fileUri)
                val sourceName = getFileName(fileUri)

                // 2. Создаем Store под конкретное имя файла
                // Важно: JsonVectorStore должен уметь принимать полный путь или имя
                val targetFile = ragRepository.getIndexFile(indexName)
                val customStore = JsonVectorStore(getApplication(), "rag_indices/$indexName.json")

                val pipeline = RagPipeline(
                    embeddingModel = embeddingService,
                    vectorStore = customStore
                )

                // 3. Запускаем
                val chunks = pipeline.ingestDocument(sourceName, content)

                // 4. Переходим в этот индекс
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