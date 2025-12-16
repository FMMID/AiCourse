package com.example.aicourse.presentation.settings.mvi

import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.mcpclient.McpClientType

sealed interface SettingsIntent {

    data class SaveSettingsChatModel(val newSettingsChatModel: SettingsChatModel) : SettingsIntent

    data class DownloadMcpTools(val mcpClientType: McpClientType) : SettingsIntent
}