package com.example.aicourse.presentation.chat.message.tokenStats

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
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.tools.tokenComparePrevious.model.TokenUsageDiff
import com.example.aicourse.ui.theme.AiCourseTheme

/**
 * Карточка статистики использования токенов
 * Отображается после каждого BOT сообщения с tokenUsage
 */
@Composable
fun TokenStatisticsCard(
    tokenUsage: TokenUsage,
    diff: TokenUsageDiff,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = dimensionResource(R.dimen.message_item_max_width)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Заголовок
            Text(
                text = "Token Usage",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stacked Progress Bar
            StackedProgressBar(
                tokenUsage = tokenUsage,
                contextLimit = tokenUsage.maxAvailableTokens ?: 0,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Текстовая статистика
            TokenStatisticsText(
                tokenUsage = tokenUsage,
                contextLimit = tokenUsage.maxAvailableTokens ?: 0,
                diff = diff,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun TokenStatisticsCardPreview() {
    AiCourseTheme {
        TokenStatisticsCard(
            tokenUsage = TokenUsage(
                promptTokens = 240,
                completionTokens = 450,
                totalTokens = 690,
                maxAvailableTokens = 8192
            ),
            diff = TokenUsageDiff(
                promptTokensDiff = 120,
                completionTokensDiff = 150,
                totalTokensDiff = 270
            )
        )
    }
}
