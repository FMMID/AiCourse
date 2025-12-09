package com.example.aicourse.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aicourse.R
import com.example.aicourse.domain.chat.model.plain.PlainTextPrompt
import com.example.aicourse.presentation.chat.message.MessagesList
import com.example.aicourse.ui.theme.AiCourseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(ChatIntent.ClearHistory) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_history_description),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = dimensionResource(R.dimen.screen_padding_top))
        ) {
            if (uiState.activePrompt !is PlainTextPrompt) {
                ActivePromptIndicator(
                    activePrompt = uiState.activePrompt,
                    onReset = { viewModel.handleIntent(ChatIntent.ResetPrompt) },
                    tokenUsage = uiState.lastTokenUsage,
                    modelName = uiState.lastModelName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.spacing_normal))
                )
            }

            MessagesList(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                listState = listState
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(R.dimen.spacing_normal),
                            vertical = dimensionResource(R.dimen.spacing_small)
                        ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            MessageInputField(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    viewModel.handleIntent(ChatIntent.SendMessage(messageText))
                    messageText = ""
                },
                enabled = !uiState.isLoading
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    AiCourseTheme {
        ChatScreen()
    }
}
