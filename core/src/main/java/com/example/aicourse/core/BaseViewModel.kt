package com.example.aicourse.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<STATE, INTENT>(
    application: Application,
    initSate: STATE
) : AndroidViewModel(application) {

    protected val _uiState = MutableStateFlow(initSate)
    val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    abstract fun handleIntent(intent: INTENT)
}