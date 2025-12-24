package com.example.aicourse.presentation.settings.mvi

import com.example.aicourse.BuildConfig
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.mcpclient.McpClientConfig
import io.modelcontextprotocol.kotlin.sdk.types.Tool

data class SettingsUiState(
    val settingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        historyStrategy = HistoryStrategy.ONE_MESSAGE,
        outPutDataStrategy = OutPutDataStrategy.None,
    ),
    val availableMcpClients: List<McpClientConfig> = listOf(
        BuildConfig.MCP_NOTIFICATION_URL,
        BuildConfig.MCP_NOTE_URL
    ).map { McpClientConfig(it) },
    val downloadedMcpClientTools: Map<McpClientConfig, List<Tool>> = mapOf()
)
