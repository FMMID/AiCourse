package com.example.aicourse.presentation.chat.message.tokenStats

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString.Builder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.tools.tokenComparePrevious.model.TokenUsageDiff
import com.example.aicourse.domain.tools.tokenComparePrevious.TokenStatisticsCalculator
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
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = baseColor)) {
            append("Input: ")
        }

        // Input tokens с цветовым кодированием diff
        appendWithDiffColor(
            value = promptTokens,
            diff = diff.promptTokensDiff,
            showDiff = showDiff,
            baseColor = baseColor
        )

        withStyle(SpanStyle(color = baseColor)) {
            append(" | Output: ")
        }

        // Output tokens с цветовым кодированием diff
        appendWithDiffColor(
            value = completionTokens,
            diff = diff.completionTokensDiff,
            showDiff = showDiff,
            baseColor = baseColor
        )

        withStyle(SpanStyle(color = baseColor)) {
            append(" | Total: ")
            append("$totalTokens/$contextLimit")
            append(" (")
            append(TokenStatisticsCalculator.formatPercentage(totalTokens, contextLimit))
            append(")")
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace
        ),
        modifier = modifier
    )
}

/**
 * Вспомогательная функция для добавления текста с цветовым кодированием diff
 */
private fun Builder.appendWithDiffColor(
    value: Int,
    diff: Int,
    showDiff: Boolean,
    baseColor: Color
) {
    val formatted = TokenStatisticsCalculator.formatWithDiff(value, diff, showDiff)

    if (!showDiff || diff == 0) {
        withStyle(SpanStyle(color = baseColor)) {
            append(formatted)
        }
    } else {
        val parts = formatted.split(" (", ")")
        val mainValue = parts[0]
        val diffPart = if (parts.size > 1) parts[1] else null

        withStyle(SpanStyle(color = baseColor)) {
            append(mainValue)
        }

        if (diffPart != null) {
            val diffColor = if (diff > 0) Color(0xFF4CAF50) else Color(0xFFE53935)
            withStyle(SpanStyle(color = diffColor)) {
                append(" ($diffPart)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TokenStatisticsTextPositPreview() {
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
                completionTokensDiff = -150,
                totalTokensDiff = 270
            )
        )
    }
}

