package com.example.aicourse.rag.presentation

import androidx.compose.foundation.background
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
        Column(modifier = Modifier.padding(12.dp)) {

            // Заголовок: Источник
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 1. Текстовый чанк
            Text(
                text = "TEXT CHUNK:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chunk.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Векторное представление (Превью)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VECTOR REPRESENTATION (size: ${chunk.embedding?.size ?: 0}):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Показываем первые 5 значений вектора, чтобы было похоже на "матрицу"
            val vectorPreview = chunk.embedding?.take(5)?.joinToString(", ") {
                "%.4f".format(it)
            } ?: "Waiting for embeddings..."

            Text(
                text = "[$vectorPreview, ...]",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF2E7D32), // Зеленый цвет "как в терминале"
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )
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