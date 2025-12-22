package com.example.aicourse.rag.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.rag.domain.model.DocumentChunk

@Composable
fun RagIndexDetail(
    chunks: List<DocumentChunk>,
    error: String? = null
) {
    if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Всего фрагментов: ${chunks.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(chunks) { chunk ->
            ChunkProcessingCard(chunk = chunk)
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
        RagIndexDetail(chunks = mockChunks)
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun RagIndexDetailErrorPreview() {
    MaterialTheme {
        RagIndexDetail(
            chunks = emptyList(),
            error = "Не удалось загрузить файл индекса. Возможно, он поврежден."
        )
    }
}