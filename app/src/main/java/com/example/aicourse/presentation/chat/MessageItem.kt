package com.example.aicourse.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.R
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun MessageItem(message: Message) {
    val isUser = message.type == MessageType.USER
    val semanticDescription = stringResource(
        if (isUser) R.string.user_message_description else R.string.bot_message_description,
        message.text
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.message_item_padding_horizontal),
                vertical = dimensionResource(R.dimen.message_item_padding_vertical)
            )
            .semantics {
                contentDescription = semanticDescription
            },
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
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
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageItemPreview() {
    AiCourseTheme {
        Column {
            MessageItem(
                message = Message(
                    id = "1",
                    text = "Привет! сообщение от пользователя",
                    type = MessageType.USER
                )
            )
            MessageItem(
                message = Message(
                    id = "2",
                    text = "Здравствуйте! ответ от бота",
                    type = MessageType.BOT
                )
            )
        }
    }
}
