package com.example.aicourse.presentation.chat.message.tokenStats

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.model.TokenUsageDiff
import com.example.aicourse.domain.chat.util.TokenStatisticsCalculator
import com.example.aicourse.ui.theme.AiCourseTheme

/**
 * Текстовое отображение статистики токенов
 * Формат: "Input: 240 (+120) | Output: 450 (+150) | Total: 690/8192 (8.4%)"
 */
@Composable
fun TokenStatisticsText(
    tokenUsage: TokenUsage,
    contextLimit: Int,
    diff: TokenUsageDiff,
    modifier: Modifier = Modifier
) {
    val promptTokens = tokenUsage.promptTokens ?: 0
    val completionTokens = tokenUsage.completionTokens ?: 0
    val totalTokens = promptTokens + completionTokens

    val showDiff = diff.hasChanges()

    val text = buildString {
        append("Input: ")

        append(
            TokenStatisticsCalculator.formatWithDiff(
                promptTokens,
                diff.promptTokensDiff,
                showDiff
            )
        )

        append(" | Output: ")
        append(
            TokenStatisticsCalculator.formatWithDiff(
                completionTokens,
                diff.completionTokensDiff,
                showDiff
            )
        )

        append(" | Total: ")
        append("$totalTokens/$contextLimit")
        append(" (")
        append(TokenStatisticsCalculator.formatPercentage(totalTokens, contextLimit))
        append(")")
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun TokenStatisticsTextPreview() {
    AiCourseTheme {
        TokenStatisticsText(
            tokenUsage = TokenUsage(
                promptTokens = 240,
                completionTokens = 450,
                totalTokens = 690
            ),
            contextLimit = 8192,
            diff = TokenUsageDiff(
                promptTokensDiff = 120,
                completionTokensDiff = 150,
                totalTokensDiff = 270
            )
        )
    }
}
