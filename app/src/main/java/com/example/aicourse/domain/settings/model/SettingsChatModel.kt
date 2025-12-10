package com.example.aicourse.domain.settings.model

data class SettingsChatModel(
    val currentUseApiImplementation: ApiImplementation,
    val isUseMessageHistory: Boolean,
    val calculateTokenCost: Boolean
)