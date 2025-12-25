package com.example.aicourse.rag.presentation

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RagIndexList(
    indices: List<String>,
    selectedIds: Set<String>,
    onIndexClick: (String) -> Unit,
    onIndexLongClick: (String) -> Unit,
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
            Text(text = "Нет созданных индексов", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(indices) { name ->
                val isSelected = selectedIds.contains(name)

                // Меняем цвет карточки при выделении
                val cardColors = if (isSelected) {
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    CardDefaults.cardColors()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(CardDefaults.shape)
                        .combinedClickable(
                            onClick = { onIndexClick(name) },
                            onLongClick = { onIndexLongClick(name) }
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
                    colors = cardColors
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Меняем иконку: Галочка если выбрано, Папка если нет
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { indexToDelete = name }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
            onDeleteClick = {},
            selectedIds = setOf("History_of_Rome"),
            onIndexLongClick = { },
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
            onDeleteClick = {},
            selectedIds = setOf(),
            onIndexLongClick = { },
        )
    }
}