package com.example.aicourse.domain.settings.model

sealed interface OutPutDataStrategy {

    data object None : OutPutDataStrategy

    class Token(val tokenConsumptionMode: TokenConsumptionMode) : OutPutDataStrategy

    class ModelInfo() : OutPutDataStrategy
}