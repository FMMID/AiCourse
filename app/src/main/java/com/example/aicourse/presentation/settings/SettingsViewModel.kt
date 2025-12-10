package com.example.aicourse.presentation.settings

import android.app.Application
import com.example.aicourse.domain.settings.usecase.SettingsUseCase
import com.example.aicourse.presentation.base.BaseViewModel

//TODO после прикрутки DI сделать прокидывание единственного инстанса SettingsUseCase
class SettingsViewModel(
    application: Application,
    private val settingsUseCase: SettingsUseCase = SettingsUseCase()
) : BaseViewModel<SettingsUiState, SettingsIntent>(application, SettingsUiState()) {

    //TODO добавить получение актуального состояния settingsChatModel из settingsUseCase для фомирования начального состояния  SettingsUiState()

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SaveSettingsChatModel -> TODO("Реализовать сохранение новых настроек в settingsUseCase + обновить локальный ui state")
        }
    }
}