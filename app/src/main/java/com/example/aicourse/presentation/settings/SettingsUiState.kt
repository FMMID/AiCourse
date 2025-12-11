package com.example.aicourse.presentation.settings

import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.model.TokenConsumptionStrategy

data class SettingsUiState(
    val settingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        historyStrategy = HistoryStrategy.PAIN,
        tokenConsumptionStrategy = TokenConsumptionStrategy.NONE,
    )
)