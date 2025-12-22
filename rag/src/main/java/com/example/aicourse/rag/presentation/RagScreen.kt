package com.example.aicourse.rag.presentation

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aicourse.rag.domain.model.DocumentChunk

@Composable
fun RagScreen(
    viewModel: RagViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    // Вызываем Stateless контент и передаем туда данные и обработчики
    RagScreenContent(
        state = state,
        onAddFileClick = { filePickerLauncher.launch("text/*") }
    )
}

@Composable
fun RagScreenContent(
    state: RagUiState,
    onAddFileClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddFileClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Загрузить файл") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "RAG Ingestion Preview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Разбивка и векторизация...", modifier = Modifier.padding(top = 64.dp))
                }
            } else if (state.error != null) {
                Text(
                    text = "Ошибка: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (state.processedChunks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Выберите файл для индексации",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Обработано чанков: ${state.processedChunks.size}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    items(state.processedChunks) { chunk ->
                        ChunkProcessingCard(chunk)
                    }
                }
            }
        }
    }
}

@Preview(name = "Mock RagScreen - Empty", showBackground = true)
@Composable
private fun MockRagScreenEmptyPreview() {
    MaterialTheme {
        RagScreenContent(
            state = RagUiState(
                isLoading = false,
                processedChunks = emptyList(),
                error = null
            ),
            onAddFileClick = {}
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun RagScreenEmptyPreview() {
    MaterialTheme {
        RagScreenContent(
            state = RagUiState(
                isLoading = false,
                processedChunks = emptyList(),
                error = null
            ),
            onAddFileClick = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun RagScreenLoadingPreview() {
    MaterialTheme {
        RagScreenContent(
            state = RagUiState(
                isLoading = true,
                processedChunks = emptyList(),
                error = null
            ),
            onAddFileClick = {}
        )
    }
}

@Preview(name = "Data State", showBackground = true)
@Composable
private fun RagScreenDataPreview() {
    val mockChunks = listOf(
        DocumentChunk(
            id = "1",
            text = "Меня зовут Gemini, и я помогаю тебе с кодом для RAG пайплайна.",
            source = "bio.txt",
            embedding = listOf(0.12f, -0.45f, 0.88f, 0.01f, -0.99f, 0.55f)
        ),
        DocumentChunk(
            id = "2",
            text = "Векторные базы данных позволяют искать информацию по смыслу, а не только по ключевым словам.",
            source = "rag_intro.md",
            embedding = listOf(-0.11f, 0.22f, -0.33f, 0.44f, -0.55f)
        )
    )

    MaterialTheme {
        RagScreenContent(
            state = RagUiState(
                isLoading = false,
                processedChunks = mockChunks,
                error = null
            ),
            onAddFileClick = {}
        )
    }
}