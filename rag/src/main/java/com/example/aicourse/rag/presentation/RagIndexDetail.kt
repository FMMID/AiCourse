package com.example.aicourse.rag.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.rag.domain.model.DocumentChunk

@Composable
fun RagIndexDetail(
    chunks: List<DocumentChunk>,
    onSearch: (String) -> Unit,
    error: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // --- 1. Строка Поиска ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Семантический поиск (RAG)...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onSearch("") // Сброс поиска: передаем пустую строку
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            // Настройка клавиатуры: кнопка "Лупа" (Search)
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch(searchQuery) // Запуск поиска
                focusManager.clearFocus() // Скрываем клавиатуру
            })
        )

        // --- 2. Контент (Список или Ошибка) ---
        if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Заголовок с информацией
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Найдено фрагментов: ${chunks.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Если поиск активен, покажем подсказку
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "Сортировка по релевантности",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                items(chunks, key = { it.id }) { chunk ->
                    ChunkProcessingCard(chunk = chunk)
                }
            }
        }
    }
}

@Preview(name = "Detail Content", showBackground = true)
@Composable
private fun RagIndexDetailPreview() {
    val mockChunks = listOf(
        DocumentChunk(
            id = "1",
            text = "Первое правило бойцовского клуба: никому не рассказывать о бойцовском клубе.",
            source = "rules.txt",
            embedding = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f)
        ),
        DocumentChunk(
            id = "2",
            text = "Второе правило бойцовского клуба: никому никогда не рассказывать о бойцовском клубе.",
            source = "rules.txt",
            embedding = listOf(0.9f, 0.8f, 0.7f, 0.6f, 0.5f)
        )
    )

    MaterialTheme {
        RagIndexDetail(chunks = mockChunks, onSearch = {})
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun RagIndexDetailErrorPreview() {
    MaterialTheme {
        RagIndexDetail(
            chunks = emptyList(),
            error = "Не удалось загрузить файл индекса. Возможно, он поврежден.",
            onSearch = {}
        )
    }
}