package com.example.aicourse.presentation.settings.mvi

import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.mcpclient.McpClientType
import io.modelcontextprotocol.kotlin.sdk.types.Tool

data class SettingsUiState(
    val settingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        historyStrategy = HistoryStrategy.PAIN,
        outPutDataStrategy = OutPutDataStrategy.None,
    ),
    val availableMcpClients: List<McpClientType> = McpClientType.entries,
    val downloadedMcpClientToos: Map<McpClientType, List<Tool>> = mapOf()
)
