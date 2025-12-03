package com.example.aicourse.presentation.chat.message.plainText

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.R
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun PlainTextCard(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(max = dimensionResource(R.dimen.message_item_max_width))
            .background(
                color = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.message_bubble_corner_radius),
                    topEnd = dimensionResource(R.dimen.message_bubble_corner_radius),
                    bottomStart = if (isUser) {
                        dimensionResource(R.dimen.message_bubble_corner_radius)
                    } else {
                        dimensionResource(R.dimen.message_bubble_corner_radius_small)
                    },
                    bottomEnd = if (isUser) {
                        dimensionResource(R.dimen.message_bubble_corner_radius_small)
                    } else {
                        dimensionResource(R.dimen.message_bubble_corner_radius)
                    }
                )
            )
            .padding(dimensionResource(R.dimen.message_bubble_padding))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}

@Preview(showBackground = true, name = "User Message")
@Composable
fun PlainTextCardUserPreview() {
    AiCourseTheme {
        PlainTextCard(
            text = "Привет! Это сообщение от пользователя",
            isUser = true
        )
    }
}

@Preview(showBackground = true, name = "Bot Message")
@Composable
fun PlainTextCardBotPreview() {
    AiCourseTheme {
        PlainTextCard(
            text = "Здравствуйте! Это ответ от бота с более длинным текстом для демонстрации",
            isUser = false
        )
    }
}