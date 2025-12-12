package com.example.aicourse.presentation.chat.message.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Системное сообщение в чате
 * Отображается по центру с иконкой, используется для уведомлений
 * о событиях суммаризации контекста и других системных операциях
 */
@Composable
fun SystemMessageCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "System message",
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(name = "System Message - Summarization")
@Composable
private fun SystemMessageCardSummarizationPreview() {
    MaterialTheme {
        SystemMessageCard(
            text = "Часть истории диалога была суммаризирована для экономии токенов"
        )
    }
}

@Preview(name = "System Message - Short")
@Composable
private fun SystemMessageCardShortPreview() {
    MaterialTheme {
        SystemMessageCard(
            text = "Контекст обновлен"
        )
    }
}

@Preview(name = "System Message - Long")
@Composable
private fun SystemMessageCardLongPreview() {
    MaterialTheme {
        SystemMessageCard(
            text = "Система выполнила автоматическую оптимизацию контекста. Старые сообщения были сжаты."
        )
    }
}

