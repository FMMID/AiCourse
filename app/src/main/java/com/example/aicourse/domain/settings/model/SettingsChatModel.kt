package com.example.aicourse.domain.settings.model

data class SettingsChatModel(
    val currentUseApiImplementation: ApiImplementation,
    val historyStrategy: HistoryStrategy,
    val tokenConsumptionStrategy: TokenConsumptionStrategy
)