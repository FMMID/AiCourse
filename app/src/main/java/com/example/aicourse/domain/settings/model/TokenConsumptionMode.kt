package com.example.aicourse.domain.settings.model

import kotlinx.serialization.Serializable

@Serializable
enum class TokenConsumptionMode {

    //TODO работает в режиме: подсчета текущего потребления токенов в запросе + сравнение с прошлым потрблением - отображение диффа
    COMPARE_PREVIOUS,

    //TODO показывает нагруженность контекстного окна (NewTotal = (SizeOfSummary) = (SizeOfBuffer) = (SizeOfSystemPrompt))
    CONTEXT_MODE
}