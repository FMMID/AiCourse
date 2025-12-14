package com.example.aicourse.domain.settings.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingsChatModel(
    val currentUseApiImplementation: ApiImplementation,
    val historyStrategy: HistoryStrategy,
    val outPutDataStrategy: OutPutDataStrategy,
)