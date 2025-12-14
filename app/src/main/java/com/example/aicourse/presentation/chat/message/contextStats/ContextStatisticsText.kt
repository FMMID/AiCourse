package com.example.aicourse.presentation.chat.message.contextStats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        if (contextWindowInfo.wasSummarized) {
            SummarizationBadge()
            Spacer(modifier = Modifier.height(4.dp))
        }
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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
}

@Composable
private fun SummarizationBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFFC107).copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Summarized",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Summarized",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFFFC107)
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
