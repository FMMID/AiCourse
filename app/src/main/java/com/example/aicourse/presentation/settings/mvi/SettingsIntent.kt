package com.example.aicourse.presentation.settings.mvi

import com.example.aicourse.domain.settings.model.SettingsChatModel

sealed interface SettingsIntent {

    data class SaveSettingsChatModel(val newSettingsChatModel: SettingsChatModel) : SettingsIntent
}