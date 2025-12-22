package com.example.aicourse.rag.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CreateIndexDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var indexName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое хранилище") },
        text = {
            OutlinedTextField(
                value = indexName,
                onValueChange = {
                    // Запрещаем пробелы и спецсимволы для простоты файловой системы
                    if (it.all { char -> char.isLetterOrDigit() || char == '_' }) {
                        indexName = it
                    }
                },
                label = { Text("Название (латиница, без пробелов)") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (indexName.isNotBlank()) {
                        onConfirm(indexName)
                    }
                },
                enabled = indexName.isNotBlank()
            ) {
                Text("Выбрать файл...")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Preview(name = "Create Dialog", showBackground = true)
@Composable
private fun CreateIndexDialogPreview() {
    CreateIndexDialog(
        onDismiss = {},
        onConfirm = {}
    )
}