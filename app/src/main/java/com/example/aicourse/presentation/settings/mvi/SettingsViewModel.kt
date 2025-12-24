package com.example.aicourse.presentation.settings.mvi

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.aicourse.domain.settings.usecase.GetLocalMcpToolsUseCase
import com.example.aicourse.domain.settings.usecase.SettingsChatUseCase
import com.example.aicourse.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsChatUseCase: SettingsChatUseCase,
    private val getLocalMcpToolsUseCase: GetLocalMcpToolsUseCase
) : BaseViewModel<SettingsUiState, SettingsIntent>(application, SettingsUiState()) {

    //TODO добавить получение актуального состояния settingsChatModel из settingsUseCase для фомирования начального состояния  SettingsUiState()

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SaveSettingsChatModel -> TODO("Реализовать сохранение новых настроек в settingsUseCase + обновить локальный ui state")
            is SettingsIntent.DownloadMcpTools -> viewModelScope.launch {
                getLocalMcpToolsUseCase(intent.mcpClientConfig).onSuccess { tools ->
                    _uiState.update { state ->
                        val typeAndTools = intent.mcpClientConfig to tools
                        val updatedDownloadedMcpClientTools = state.downloadedMcpClientTools + typeAndTools
                        state.copy(downloadedMcpClientTools = updatedDownloadedMcpClientTools)
                    }
                }
            }
        }
    }
}