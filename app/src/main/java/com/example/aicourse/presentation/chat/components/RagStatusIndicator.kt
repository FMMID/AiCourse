package com.example.aicourse.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.domain.chat.model.RagMode
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun RagStatusIndicator(
    ragMode: RagMode,
    ragIndexId: String?,
    modifier: Modifier = Modifier
) {
    if (ragIndexId.isNullOrBlank()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = RagUiUtils.getRagColor(ragMode),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = when (ragMode) {
                RagMode.DISABLED -> "RAG выключен"
                RagMode.STANDARD -> "Индекс: $ragIndexId"
                RagMode.WITH_RERANKER -> "Умный поиск: $ragIndexId"
                RagMode.WITH_MULTIQUERY -> "Премиум: $ragIndexId"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RagStatusIndicatorPreview() {
    AiCourseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RagStatusIndicator(
                ragMode = RagMode.WITH_RERANKER,
                ragIndexId = "course_docs_v1"
            )
        }
    }
}