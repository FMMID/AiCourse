package com.example.aicourse.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.presentation.chat.message.MessagesList
import com.example.aicourse.presentation.chat.mvi.ChatIntent
import com.example.aicourse.presentation.chat.mvi.ChatUiState
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.uiKit.MessageInputField
import com.example.aicourse.ui.theme.AiCourseTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    ragIndexId: String,
    viewModel: ChatViewModel
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
            ChatTopBar(
                state = uiState,
                ragIndexId = ragIndexId,
                onBackClick = { navController.popBackStack() },
                onToggleRag = { viewModel.handleIntent(ChatIntent.ToggleRagMode) },
                onClearClick = { viewModel.handleIntent(ChatIntent.ClearHistory) }
            )
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
        },
        bottomBar = {
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
                    tokenUsage = uiState.messages.lastOrNull()?.tokenUsage,
                    toolResult = uiState.messages.lastOrNull()?.toolResult,
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    state: ChatUiState,
    ragIndexId: String,
    onBackClick: () -> Unit,
    onToggleRag: () -> Unit,
    onClearClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.chat_title))
                if (state.isRagModeEnabled && ragIndexId.isNotEmpty()) {
                    Text(
                        text = "RAG: $ragIndexId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        },
        actions = {
            if (state.showRagButton) {
                IconToggleButton(
                    checked = state.isRagModeEnabled,
                    onCheckedChange = { onToggleRag() }
                ) {
                    val icon = if (state.isRagModeEnabled) {
                        Icons.AutoMirrored.Filled.LibraryBooks
                    } else {
                        Icons.AutoMirrored.Outlined.LibraryBooks
                    }

                    val tint = if (state.isRagModeEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = if (state.isRagModeEnabled) "Выключить RAG" else "Включить RAG",
                        tint = tint
                    )
                }
            } else {
                IconButton(onClick = { onClearClick() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.clear_history_description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    AiCourseTheme {
        ChatScreen(
            navController = TODO(),
            ragIndexId = TODO(),
            viewModel = TODO()
        )
    }
}
