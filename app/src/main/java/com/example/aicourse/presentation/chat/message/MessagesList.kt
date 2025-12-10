package com.example.aicourse.presentation.chat.message

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun MessagesList(
    modifier: Modifier = Modifier,
    messages: List<Message> = emptyList(),
    isLoading: Boolean = false,
    listState: LazyListState = rememberLazyListState()
) {
    val listDescription = stringResource(R.string.message_list_description)

    LazyColumn(
        modifier = modifier.semantics {
            contentDescription = listDescription
        },
        state = listState,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        items(
            items = messages,
            key = { message -> message.id }
        ) { message ->
            MessageItem(message = message)
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.loading_indicator_padding)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_normal))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesListPreview() {
    AiCourseTheme {
        MessagesList(
            modifier = Modifier.fillMaxWidth(),
            messages = listOf(
                Message(
                    id = "1",
                    text = "User 1",
                    type = MessageType.USER,
                    timestamp = 0
                ),
                Message(
                    id = "2",
                    text = "Bot 1",
                    type = MessageType.BOT,
                    timestamp = 0
                ),
                Message(
                    id = "3",
                    text = "User 2",
                    type = MessageType.USER,
                    timestamp = 0
                ),
                Message(
                    id = "4",
                    text = "Bot 2",
                    type = MessageType.BOT,
                    timestamp = 0
                ),
            )
        )
    }
}