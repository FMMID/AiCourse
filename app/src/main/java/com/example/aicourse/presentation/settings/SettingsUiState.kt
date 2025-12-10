package com.example.aicourse.presentation.settings

import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.SettingsChatModel

data class SettingsUiState(
    val settingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        isUseMessageHistory = true,
        calculateTokenCost = true,
    )
)