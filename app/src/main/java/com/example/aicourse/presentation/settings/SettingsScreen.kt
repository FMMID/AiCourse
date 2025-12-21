package com.example.aicourse.presentation.settings

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aicourse.BuildConfig
import com.example.aicourse.R
import com.example.aicourse.di.AppInjector
import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.presentation.settings.mcpTools.McpClientSection
import com.example.aicourse.presentation.settings.mvi.SettingsIntent
import com.example.aicourse.presentation.settings.mvi.SettingsUiState
import com.example.aicourse.presentation.settings.mvi.SettingsViewModel
import com.example.aicourse.presentation.settings.mvi.SettingsViewModelFactory
import com.example.aicourse.ui.theme.AiCourseTheme
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Settings screen for configuring chat parameters and managing MCP client tools.
 * Provides UI for downloading and viewing available tools from MCP clients.
 *
 * @param viewModel The ViewModel managing settings state and MCP client operations
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val settingsUiState by viewModel.uiState.collectAsState()
    SettingsScreenContent(
        settingsUiState = settingsUiState,
        onIntent = viewModel::handleIntent
    )
}

/**
 * Stateless content for the settings screen.
 *
 * @param settingsUiState Current UI state containing MCP client tools and settings
 * @param onIntent Handler for user intents (tool downloads, settings changes)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    settingsUiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit = {}
) {
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
                    Text(
                        text = stringResource(R.string.mcp_section_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
                    )
                }

                items(
                    items = settingsUiState.availableMcpClients,
                    key = { it.serverUrl }
                ) { mcpClientConfig ->
                    McpClientSection(
                        mcpClientConfig = mcpClientConfig,
                        tools = settingsUiState.downloadedMcpClientTools[mcpClientConfig],
                        onDownloadClick = { type ->
                            onIntent(SettingsIntent.DownloadMcpTools(type))
                        },
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.spacing_normal),
                            vertical = dimensionResource(R.dimen.spacing_small)
                        )
                    )
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
            settingsUiState = SettingsUiState(
                availableMcpClients = AppInjector.availableMcpConfigs,
                downloadedMcpClientTools = mapOf(
                    McpClientConfig(BuildConfig.MCP_NOTIFICATION_URL) to listOf(
                        Tool(
                            name = "get_weather",
                            title = "Get Weather",
                            description = "Reads weather data for a location",
                            inputSchema = ToolSchema(
                                properties = buildJsonObject {
                                    put("type", "object")
                                    putJsonObject("properties") {
                                        putJsonObject("location") {
                                            put("type", "string")
                                            put("description", "City name or coordinates")
                                        }
                                    }
                                }
                            )
                        )
                    )
                )
            )
        )
    }
}
