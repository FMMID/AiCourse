package com.example.aicourse.domain.tools.context.model

data class ContextWindow(
    val originalLimit: Int,
    val summeryThreshold: Float = 0.8f
) {

    fun shouldSummeryDialog(historyTokens: Int): Boolean {
        return historyTokens < originalLimit * summeryThreshold
    }
}