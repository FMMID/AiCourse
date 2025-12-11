package com.example.aicourse.domain.settings.model

enum class TokenConsumptionStrategy {

    //TODO никакой информации по токенам не выводится
    NONE,

    //TODO работает в режиме: подсчета текущего потребления токенов в запросе + сравнение с прошлым потрблением - отображение диффа
    COMPARE_PREVIOUS,

    //TODO показывает нагруженность контекстного окна (NewTotal = (SizeOfSummary) = (SizeOfBuffer) = (SizeOfSystemPrompt))
    CONTEXT_MODE
}