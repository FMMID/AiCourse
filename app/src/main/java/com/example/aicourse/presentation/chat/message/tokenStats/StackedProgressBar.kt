package com.example.aicourse.presentation.chat.message.tokenStats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.ui.theme.AiCourseTheme
import com.example.aicourse.ui.theme.TokenUsageColors

/**
 * Горизонтальный segmented progress bar для отображения использования токенов
 * Segment A (Input): синий
 * Segment B (Output): зеленый
 * Segment C (Free Space): серый
 */
@Composable
fun StackedProgressBar(
    tokenUsage: TokenUsage,
    contextLimit: Int,
    modifier: Modifier = Modifier
) {
    val promptTokens = tokenUsage.promptTokens ?: 0
    val completionTokens = tokenUsage.completionTokens ?: 0
    val total = promptTokens + completionTokens
    val freeSpace = maxOf(0, contextLimit - total)

    // Вычисляем веса для каждого сегмента
    val totalForWeights = maxOf(1, contextLimit) // Защита от деления на 0
    val inputWeight = promptTokens.toFloat() / totalForWeights
    val outputWeight = completionTokens.toFloat() / totalForWeights
    val freeWeight = freeSpace.toFloat() / totalForWeights

    Row(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Segment A: Input Tokens (Синий)
        if (inputWeight > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(inputWeight)
                    .background(TokenUsageColors.InputTokens)
            )
        }

        // Segment B: Output Tokens (Зеленый)
        if (outputWeight > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(outputWeight)
                    .background(TokenUsageColors.OutputTokens)
            )
        }

        // Segment C: Free Space (Серый)
        if (freeWeight > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(freeWeight)
                    .background(TokenUsageColors.FreeSpace)
            )
        }
    }
}

@Preview
@Composable
private fun StackedProgressBarPreview() {
    AiCourseTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 8.4% usage
            StackedProgressBar(
                tokenUsage = TokenUsage(
                    promptTokens = 240,
                    completionTokens = 450,
                    totalTokens = 690
                ),
                contextLimit = 8192
            )

            // 50% usage
            StackedProgressBar(
                tokenUsage = TokenUsage(
                    promptTokens = 2048,
                    completionTokens = 2048,
                    totalTokens = 4096
                ),
                contextLimit = 8192
            )

            // 90% usage
            StackedProgressBar(
                tokenUsage = TokenUsage(
                    promptTokens = 3686,
                    completionTokens = 3686,
                    totalTokens = 7372
                ),
                contextLimit = 8192
            )
        }
    }
}
