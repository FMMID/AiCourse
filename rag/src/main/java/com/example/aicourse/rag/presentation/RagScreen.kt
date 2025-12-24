package com.example.aicourse.rag.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RagScreen(
    viewModel: RagViewModel,
    onIndexSelected: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showCreateDialog) {
        CreateIndexDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onConfirm = { name, fileUri -> viewModel.createNewIndex(name, fileUri) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.selectedIndexName != null) {
                        Text(state.selectedIndexName!!)
                    } else if (state.chatTargetId != null) {
                        Text("Выбрано: ${state.chatTargetId}")
                    } else {
                        Text("Базы знаний RAG")
                    }
                },
                navigationIcon = {
                    if (state.selectedIndexName != null) {
                        IconButton(onClick = { viewModel.onBackToList() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    } else if (state.chatTargetId != null) {
                        // При выборе тоже можно показать кнопку "Сброс" или просто Назад
                        IconButton(onClick = { viewModel.clearChatSelection() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Снять выделение")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.chatTargetId != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onIndexSelected("rag_indices/${state.chatTargetId!!}.json")
                        viewModel.clearChatSelection()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Chat") },
                    text = { Text("Начать чат") }
                )
            } else if (state.selectedIndexName == null && !state.isLoading) {
                FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Создать")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (state.selectedIndexName == null) {
                    RagIndexList(
                        indices = state.availableIndices,
                        selectedId = state.chatTargetId,
                        onIndexClick = { viewModel.onIndexSelected(it) },
                        onIndexLongClick = { viewModel.onIndexLongClicked(it) },
                        onDeleteClick = { viewModel.deleteIndex(it) }
                    )
                } else {
                    RagIndexDetail(
                        chunks = state.processedChunks,
                        error = state.error,
                        onSearch = { query -> viewModel.onSearchQuery(query) }
                    )
                }
            }
        }
    }
}
