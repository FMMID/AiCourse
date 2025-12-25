package com.example.aicourse.rag.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicourse.rag.domain.RagPipeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(
    application: Application,
    private val ragPipeline: RagPipeline
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadIndicesList()
    }

    fun onIndexClicked(name: String) {
        val currentSelection = _uiState.value.selectedIndicesForChat

        if (currentSelection.isNotEmpty()) {
            // Если режим выбора активен (выбран хотя бы один), обычный клик работает как переключение
            toggleSelection(name)
        } else {
            // Иначе открываем детали индекса
            openIndexDetails(name)
        }
    }

    fun onIndexLongClicked(name: String) {
        toggleSelection(name)
    }

    private fun toggleSelection(name: String) {
        val currentSelection = _uiState.value.selectedIndicesForChat.toMutableSet()
        if (currentSelection.contains(name)) {
            currentSelection.remove(name)
        } else {
            currentSelection.add(name)
        }
        _uiState.value = _uiState.value.copy(selectedIndicesForChat = currentSelection)
    }

    fun clearChatSelection() {
        _uiState.value = _uiState.value.copy(selectedIndicesForChat = emptySet())
    }

    private fun openIndexDetails(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Загружаем контекст только одного файла для просмотра
            ragPipeline.loadActiveContext(listOf(name))
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedIndexName = name,
                processedChunks = ragPipeline.activeKnowledgeBase
            )
        }
    }

    fun onSearchQuery(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
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
        _uiState.value = _uiState.value.copy(
            selectedIndexName = null,
            processedChunks = emptyList(),
            error = null
        )
        // При возврате обновляем список, вдруг что-то удалилось или добавилось
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
                val content = readFileContent(fileUri)
                val chunks = ragPipeline.ingestDocument(indexName, content)

                // Сразу открываем детали созданного
                ragPipeline.loadActiveContext(listOf(indexName))
                loadIndicesList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedIndexName = indexName,
                    processedChunks = chunks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteIndex(indexName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ragPipeline.deleteIndex(indexName)
            if (success) {
                // Если удалили то, что было выделено для чата — убираем из выделения
                if (_uiState.value.selectedIndicesForChat.contains(indexName)) {
                    val newSelection = _uiState.value.selectedIndicesForChat - indexName
                    _uiState.value = _uiState.value.copy(selectedIndicesForChat = newSelection)
                }

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
            val indices = ragPipeline.getAvailableIndices()
            _uiState.value = _uiState.value.copy(availableIndices = indices)
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