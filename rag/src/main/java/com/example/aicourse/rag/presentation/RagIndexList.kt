package com.example.aicourse.rag.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RagIndexList(
    indices: List<String>,
    onIndexClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var indexToDelete by remember { mutableStateOf<String?>(null) }

    if (indexToDelete != null) {
        AlertDialog(
            onDismissRequest = { indexToDelete = null },
            title = { Text("Удалить хранилище?") },
            text = { Text("Вы уверены, что хотите удалить индекс \"${indexToDelete}\"? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        indexToDelete?.let { onDeleteClick(it) }
                        indexToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { indexToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (indices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Нет индексов.\nНажми +, чтобы создать.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(indices) { name ->
                IndexItem(
                    name = name,
                    onClick = { onIndexClick(name) },
                    onDelete = { indexToDelete = name }
                )
            }
        }
    }
}

@Composable
private fun IndexItem(
    name: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick) // Клик по всей карточке открывает
                .padding(12.dp), // Чуть уменьшил padding, чтобы влезла кнопка
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Название занимает всё доступное место
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Кнопка удаления
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Populated List", showBackground = true)
@Composable
private fun RagIndexListPreview() {
    MaterialTheme {
        RagIndexList(
            indices = listOf("History_of_Rome", "Kotlin_Docs", "My_Secret_Plans"),
            onIndexClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(name = "Empty List", showBackground = true)
@Composable
private fun RagIndexListEmptyPreview() {
    MaterialTheme {
        RagIndexList(
            indices = emptyList(),
            onIndexClick = {},
            onDeleteClick = {}
        )
    }
}