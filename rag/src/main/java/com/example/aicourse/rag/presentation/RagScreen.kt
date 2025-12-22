package com.example.aicourse.rag.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RagScreen(
    viewModel: RagViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var pendingIndexName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.createNewIndex(pendingIndexName, it) }
    }

    if (state.showCreateDialog) {
        CreateIndexDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onConfirm = { name ->
                pendingIndexName = name
                filePickerLauncher.launch("text/*") // Запускаем выбор файла после ввода имени
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.selectedIndexName ?: "RAG Хранилища")
                },
                navigationIcon = {
                    if (state.selectedIndexName != null) {
                        IconButton(onClick = { viewModel.onBackToList() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedIndexName == null && !state.isLoading) {
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
                        onIndexClick = { viewModel.onIndexSelected(it) },
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
