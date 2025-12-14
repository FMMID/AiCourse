package com.example.aicourse.domain.settings.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface OutPutDataStrategy {

    @Serializable
    data object None : OutPutDataStrategy

    @Serializable
    data class Token(val tokenConsumptionMode: TokenConsumptionMode) : OutPutDataStrategy

    @Serializable
    data object ModelInfo : OutPutDataStrategy
}