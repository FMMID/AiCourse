package com.example.aicourse.domain.settings.usecase

import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.SettingsChatModel

//TODO добавить локальное хранилище, откуда будем загружать конфиг
class SettingsChatUseCase {

    //TODO пока храним в рантайме, как добавиться localStorage - перенесем туда
    private var activeSettingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        isUseMessageHistory = true,
        calculateTokenCost = true
    )

    fun saveSettingsChatModel(newSettingsChatModel: SettingsChatModel) {
        activeSettingsChatModel = newSettingsChatModel
    }

    fun getSettingsChatModel(): SettingsChatModel {
        return activeSettingsChatModel
    }
}