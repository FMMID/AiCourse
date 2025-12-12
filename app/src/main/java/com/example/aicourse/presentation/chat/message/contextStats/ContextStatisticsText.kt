package com.example.aicourse.presentation.chat.message.contextStats

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo

/**
 * Текстовая статистика использования контекстного окна
 * Отображает общее использование и разбивку по категориям
 */
@Composable
fun ContextStatisticsText(
    contextWindowInfo: ContextWindowInfo,
    modifier: Modifier = Modifier
) {
    val info = contextWindowInfo.contextInfo

    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append("Used: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${contextWindowInfo.totalUsedTokens}")
                }
                append(" / ${contextWindowInfo.contextLimit} tokens")
                append(" (${(contextWindowInfo.usagePercentage * 100).toInt()}%)")
            },
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = buildAnnotatedString {
                append("Summary: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${info.sizeOfSummaryMessages}")
                }
                append(" | Active: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${info.sizeOfActiveMessages}")
                }
                append(" | Prompt: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${info.sizeOfSystemPrompt}")
                }
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(name = "Statistics - Low Usage", showBackground = true)
@Composable
private fun ContextStatisticsTextLowPreview() {
    MaterialTheme {
        ContextStatisticsText(
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

@Preview(name = "Statistics - With Summarization", showBackground = true)
@Composable
private fun ContextStatisticsTextWithSummaryPreview() {
    MaterialTheme {
        ContextStatisticsText(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 500,
                    sizeOfActiveMessages = 3200,
                    sizeOfSystemPrompt = 300
                ),
                totalUsedTokens = 4000,
                contextLimit = 8000,
                usagePercentage = 0.5f,
                wasSummarized = true
            )
        )
    }
}

@Preview(name = "Statistics - High Usage", showBackground = true)
@Composable
private fun ContextStatisticsTextHighPreview() {
    MaterialTheme {
        ContextStatisticsText(
            contextWindowInfo = ContextWindowInfo(
                contextInfo = ContextInfo(
                    sizeOfSummaryMessages = 1200,
                    sizeOfActiveMessages = 4800,
                    sizeOfSystemPrompt = 500
                ),
                totalUsedTokens = 6500,
                contextLimit = 8000,
                usagePercentage = 0.81f,
                wasSummarized = true
            )
        )
    }
}
