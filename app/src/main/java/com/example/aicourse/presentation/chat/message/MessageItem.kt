package com.example.aicourse.presentation.chat.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.aicourse.domain.chat.model.TokenUsageDiff
import com.example.aicourse.domain.chat.promt.json.JsonOutputResponse
import com.example.aicourse.domain.chat.promt.pc.PcBuildResponse
import com.example.aicourse.presentation.chat.message.jsonOutput.JsonOutputCard
import com.example.aicourse.presentation.chat.message.pcBuild.PcBuildCard
import com.example.aicourse.presentation.chat.message.plainText.PlainTextCard
import com.example.aicourse.presentation.chat.message.tokenStats.TokenStatisticsCard
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun MessageItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.type == MessageType.USER
    val semanticDescription = stringResource(
        if (isUser) R.string.user_message_description else R.string.bot_message_description,
        message.text
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
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
            val typedResponse = message.typedResponse
            when {
                !isUser && typedResponse is JsonOutputResponse -> {
                    JsonOutputCard(response = typedResponse)
                }

                !isUser && typedResponse is PcBuildResponse -> {
                    PcBuildCard(response = typedResponse)
                }

                else -> {
                    PlainTextCard(
                        text = message.text,
                        isUser = isUser
                    )
                }
            }
        }

        if (!isUser && message.tokenUsage?.hasData() == true && message.tokenUsage.totalTokens != null) {
            TokenStatisticsCard(
                tokenUsage = message.tokenUsage,
                contextLimit = message.tokenUsage.totalTokens,
                diff = message.tokenUsageDiff ?: TokenUsageDiff(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.message_item_padding_horizontal),
                        end = dimensionResource(R.dimen.message_item_padding_horizontal),
                        bottom = dimensionResource(R.dimen.message_item_padding_vertical)
                    )
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
            MessageItem(
                message = Message(
                    id = "3",
                    text = "",
                    type = MessageType.BOT,
                    typedResponse = JsonOutputResponse(
                        rawContent = "",
                        isValid = true,
                        title = "Фотосинтез",
                        body = "Процесс, при котором растения используют солнечный свет для преобразования углекислого газа и воды в глюкозу и кислород."
                    )
                )
            )
            MessageItem(
                message = Message(
                    id = "3",
                    text = "",
                    type = MessageType.BOT,
                    typedResponse = JsonOutputResponse(
                        rawContent = "",
                        isValid = true,
                        error = "Контент не удалось распарсить"
                    )
                )
            )
        }
    }
}
