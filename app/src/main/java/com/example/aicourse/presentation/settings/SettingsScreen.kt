package com.example.aicourse.presentation.settings

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aicourse.R
import com.example.aicourse.presentation.settings.mvi.SettingsUiState
import com.example.aicourse.presentation.settings.mvi.SettingsViewModel
import com.example.aicourse.presentation.settings.mvi.SettingsViewModelFactory
import com.example.aicourse.ui.theme.AiCourseTheme
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val settingsUiState by viewModel.uiState.collectAsState()
    SettingsScreenContent(settingsUiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(settingsUiState: SettingsUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки чата") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = dimensionResource(R.dimen.screen_padding_top))
        ) {
            LazyColumn {
                item {
                    Text("MCP tools:")
                }

                items(settingsUiState.availableMcpTools) { tool ->
                    Text("toolName:${tool.name}")
                    HorizontalDivider()
                }
            }
        }
    }
    /**
     * TODO: реализовать UI экрана настроек:
     * 1. Сверху навбар с возвращением в чат (слева стрелка назад)
     * 2. Основной контент:
     * - Секция выбора доступного Ai Api - можно выбрать только 1 вариант ApiImplementation
     * - Секция с дополнительными параметрами:
     * - Учитывать историю диалога во время общения? (Чекбокс)
     * - Учитывать подсчет расхода токенов во время общения? (Чекбокс)
     * - Плавающая кнопка "Сохранить" - по нажатию на кнопку посылается интент в SettingsViewModel с сохранением настроек.
     */
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AiCourseTheme {
        SettingsScreenContent(
            SettingsUiState(
                availableMcpTools = listOf(
                    Tool("test1", ToolSchema()),
                    Tool("test2", ToolSchema()),
                )
            )
        )
    }
}