package com.example.aicourse.rag.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicourse.rag.domain.model.DocumentChunk

@Composable
fun ChunkProcessingCard(chunk: DocumentChunk) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        val scoreColor = when {
            chunk.score == null -> MaterialTheme.colorScheme.surfaceVariant
            chunk.score > 0.8f -> Color(0xFF4CAF50)
            chunk.score > 0.6f -> Color(0xFFFFC107)
            else -> Color(0xFFEF5350)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // --- HEADER ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Левая часть: Имя файла
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chunk.source,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Правая часть: SCORE (Показываем только если есть)
                    if (chunk.score != null) {
                        Surface(
                            color = scoreColor.copy(alpha = 0.2f),
                            contentColor = scoreColor.copy(alpha = 1f), // Делаем текст темнее фона
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Score: %.4f".format(chunk.score),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // --- TEXT ---
                Text(
                    text = chunk.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6, // Чуть больше строк, чтобы видеть контекст
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- VECTOR PREVIEW ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VECTOR (${chunk.embedding?.size ?: 0}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val vectorPreview = chunk.embedding?.take(5)?.joinToString(", ") {
                    "%.3f".format(it)
                } ?: "Waiting..."

                Text(
                    text = "[$vectorPreview, ...]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(name = "Single Card Item", showBackground = true)
@Composable
private fun ChunkCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChunkProcessingCard(
                chunk = DocumentChunk(
                    id = "1",
                    text = "Это пример отображения одной карточки с текстом и вектором.",
                    source = "example.txt",
                    embedding = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f)
                )
            )
        }
    }
}

@Preview
@Composable
fun ChunkWithScorePreview() {
    MaterialTheme {
        ChunkProcessingCard(
            chunk = DocumentChunk(
                id = "1",
                text = "Пример текста, который очень релевантен запросу пользователя.",
                source = "data.txt",
                embedding = listOf(0.1f, 0.2f),
                score = 0.8942f // Симулируем высокий скор
            )
        )
    }
}