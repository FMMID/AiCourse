package com.example.aicourse.domain.settings.usecase

import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.model.TokenConsumptionMode

//TODO добавить локальное хранилище, откуда будем загружать конфиг
class SettingsChatUseCase {

    //TODO пока храним в рантайме, как добавиться localStorage - перенесем туда
    private var activeSettingsChatModel: SettingsChatModel = SettingsChatModel(
        currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
        historyStrategy = HistoryStrategy.SUMMARIZE,
        outPutDataStrategy = OutPutDataStrategy.Token(TokenConsumptionMode.CONTEXT_MODE),
    )

    fun saveSettingsChatModel(newSettingsChatModel: SettingsChatModel) {
        activeSettingsChatModel = newSettingsChatModel
    }

    fun getSettingsChatModel(): SettingsChatModel {
        return activeSettingsChatModel
    }
}