package com.example.aicourse.presentation.chat.message.contextStats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.R
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo

/**
 * Карточка с информацией о состоянии контекстного окна
 * Отображается под сообщением бота, показывает:
 * - Прогресс-бар с цветовой индикацией
 * - Детальную статистику использования токенов
 */
@Composable
fun ContextWindowCard(
    contextWindowInfo: ContextWindowInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = dimensionResource(R.dimen.message_item_max_width)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Context Window",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContextProgressBar(
                usagePercentage = contextWindowInfo.usagePercentage,
                colorLevel = contextWindowInfo.getIndicatorColorLevel(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContextStatisticsText(
                contextWindowInfo = contextWindowInfo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Context Window - Low Usage (Green)")
@Composable
private fun ContextWindowCardLowUsagePreview() {
    MaterialTheme {
        ContextWindowCard(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 0,
                    sizeOfActiveMessages = 1200,
                    sizeOfSystemPrompt = 300
                ),
                totalUsedTokens = 1500,
                contextLimit = 8000,
                usagePercentage = 0.19f,
                wasSummarized = false
            )
        )
    }
}

@Preview(name = "Context Window - Medium Usage (Yellow)")
@Composable
private fun ContextWindowCardMediumUsagePreview() {
    MaterialTheme {
        ContextWindowCard(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 800,
                    sizeOfActiveMessages = 4200,
                    sizeOfSystemPrompt = 500
                ),
                totalUsedTokens = 5500,
                contextLimit = 8000,
                usagePercentage = 0.69f,
                wasSummarized = true
            )
        )
    }
}

@Preview(name = "Context Window - High Usage (Red)")
@Composable
private fun ContextWindowCardHighUsagePreview() {
    MaterialTheme {
        ContextWindowCard(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 1500,
                    sizeOfActiveMessages = 5200,
                    sizeOfSystemPrompt = 500
                ),
                totalUsedTokens = 7200,
                contextLimit = 8000,
                usagePercentage = 0.9f,
                wasSummarized = true,
                summarizationError = null
            )
        )
    }
}

@Preview(name = "Context Window - With Error")
@Composable
private fun ContextWindowCardWithErrorPreview() {
    MaterialTheme {
        ContextWindowCard(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 0,
                    sizeOfActiveMessages = 6500,
                    sizeOfSystemPrompt = 500
                ),
                totalUsedTokens = 6000,
                contextLimit = 8000,
                usagePercentage = 0.7f,
                wasSummarized = false,
                summarizationError = "Ошибка суммаризации: Network error"
            )
        )
    }
}
