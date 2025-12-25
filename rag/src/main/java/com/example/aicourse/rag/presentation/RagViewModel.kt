package com.example.aicourse.rag.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.rag.domain.RagPipeline
import com.example.aicourse.rag.domain.RagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(
    application: Application,
    private val ragRepository: RagRepository,
    private val ragPipeline: RagPipeline
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadIndicesList()
    }

    fun onIndexSelected(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Загружаем активный контекст в Pipeline
            // Pipeline сам сходит в репозиторий и подготовит данные для поиска
            ragPipeline.loadActiveContext(listOf(name))

            // 2. Получаем "сырые" чанки просто для отображения списка на экране (если нужно)
            // (Так как activeKnowledgeBase внутри pipeline приватный, можем взять их через репо для UI)
            val chunksForUi = ragRepository.loadIndices(listOf(name))

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedIndexName = name,
                processedChunks = chunksForUi
            )
        }
    }

    fun onMultipleIndicesSelected(names: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            ragPipeline.loadActiveContext(names)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onSearchQuery(query: String) {
        if (query.isBlank()) {
            // Если запрос пустой, просто показываем список, который был загружен
            // (Логика отображения может зависеть от ваших требований UI)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Поиск теперь идет по тому контексту, который мы загрузили через loadActiveContext
                val relevantChunks = ragPipeline.retrieve(query)

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
        // Очищаем выбор (можно добавить метод clearActiveContext в pipeline, если нужно освободить память)
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
                val sourceName = getFileName(fileUri) // Можно использовать indexName или реальное имя файла

                // Используем имя индекса как имя файла для сохранения
                // Ingest теперь внутри сохраняет через репозиторий
                val chunks = ragPipeline.ingestDocument(indexName, content)

                // Сразу делаем этот новый файл активным контекстом
                ragPipeline.loadActiveContext(listOf(indexName))

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