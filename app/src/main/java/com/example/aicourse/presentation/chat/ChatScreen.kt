package com.example.aicourse.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aicourse.R
import com.example.aicourse.rag.domain.model.RagMode
import com.example.aicourse.prompt.plain.PlainTextPrompt
import com.example.aicourse.presentation.chat.components.RagModeSelector
import com.example.aicourse.presentation.chat.components.RagStatusIndicator
import com.example.aicourse.presentation.chat.message.MessagesList
import com.example.aicourse.presentation.chat.mvi.ChatIntent
import com.example.aicourse.presentation.chat.mvi.ChatUiState
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.uiKit.MessageInputField
import com.example.aicourse.ui.theme.AiCourseTheme

// --- 1. STATEFUL COMPOSABLE (Для использования в навигации) ---
@Composable
fun ChatScreen(
    navController: NavController,
    ragIndexId: String?,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Передаем состояние и события в Stateless компонент
    ChatScreenContent(
        uiState = uiState,
        ragIndexId = ragIndexId,
        onBackClick = { navController.popBackStack() },
        onModeChange = { viewModel.handleIntent(ChatIntent.SetRagMode(it)) },
        onClearClick = { viewModel.handleIntent(ChatIntent.ClearHistory) },
        onSendMessage = { text -> viewModel.handleIntent(ChatIntent.SendMessage(text)) }
    )
}

// --- 2. STATELESS COMPOSABLE (Чистый UI, который можно тестить и превьюить) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    ragIndexId: String?,
    onBackClick: () -> Unit,
    onModeChange: (RagMode) -> Unit,
    onClearClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Авто-скролл вниз при добавлении сообщений
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
                onBackClick = onBackClick,
                onModeChange = onModeChange,
                onClearClick = onClearClick
            )
        },
        bottomBar = {
            MessageInputField(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    onSendMessage(messageText)
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
                // Если activePrompt может быть null, добавь проверку uiState.activePrompt != null
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

// --- 3. COMPONENTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    state: ChatUiState,
    ragIndexId: String?,
    onBackClick: () -> Unit,
    onModeChange: (RagMode) -> Unit,
    onClearClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.chat_title))
                // Используем новый компонент
                if (state.showRagButton) {
                    RagStatusIndicator(
                        ragMode = state.ragMode,
                        ragIndexId = ragIndexId,
                        modifier = Modifier.padding(top = 2.dp)
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
                // Компонент меню выбора режима
                RagModeSelector(
                    currentMode = state.ragMode,
                    onModeSelected = onModeChange
                )
            }

            IconButton(onClick = { onClearClick() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.clear_history_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// --- 4. PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    AiCourseTheme {
        val mockState = ChatUiState(
            messages = emptyList(),
            isLoading = false,
            ragMode = RagMode.STANDARD,
            showRagButton = true,
            error = null,
            activePrompt = PlainTextPrompt()
        )

        ChatScreenContent(
            uiState = mockState,
            ragIndexId = "course_index_v1",
            onBackClick = {},
            onClearClick = {},
            onSendMessage = {},
            onModeChange = { },
        )
    }
}