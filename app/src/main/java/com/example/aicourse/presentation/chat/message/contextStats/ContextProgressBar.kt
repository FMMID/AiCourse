package com.example.aicourse.presentation.chat.message.contextStats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo

/**
 * Прогресс-бар для отображения использования контекстного окна
 * Цвет меняется в зависимости от процента заполнения
 */
@Composable
fun ContextProgressBar(
    usagePercentage: Float,
    colorLevel: ContextWindowInfo.ColorLevel,
    modifier: Modifier = Modifier
) {
    val barColor = when (colorLevel) {
        ContextWindowInfo.ColorLevel.GREEN -> Color(0xFF4CAF50)
        ContextWindowInfo.ColorLevel.YELLOW -> Color(0xFFFFC107)
        ContextWindowInfo.ColorLevel.RED -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = usagePercentage.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(4.dp))
                .background(barColor)
        )
    }
}

@Preview(name = "Green Progress Bar (30%)")
@Composable
private fun ContextProgressBarGreenPreview() {
    MaterialTheme {
        ContextProgressBar(
            usagePercentage = 0.3f,
            colorLevel = ContextWindowInfo.ColorLevel.GREEN,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Yellow Progress Bar (75%)")
@Composable
private fun ContextProgressBarYellowPreview() {
    MaterialTheme {
        ContextProgressBar(
            usagePercentage = 0.75f,
            colorLevel = ContextWindowInfo.ColorLevel.YELLOW,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Red Progress Bar (95%)")
@Composable
private fun ContextProgressBarRedPreview() {
    MaterialTheme {
        ContextProgressBar(
            usagePercentage = 0.95f,
            colorLevel = ContextWindowInfo.ColorLevel.RED,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
