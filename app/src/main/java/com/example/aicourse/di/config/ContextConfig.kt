package com.example.aicourse.di.config

import com.example.aicourse.domain.tools.context.model.ContextWindow

/**
 * Конфигурация контекстного окна
 */
data class ContextConfig(
    val originalLimit: Int = 8000,
    val keepLastMessagesNumber: Int = 1,
    val summaryThreshold: Float = 0.4f
) {
    fun toContextWindow() = ContextWindow(
        originalLimit = originalLimit,
        keepLastMessagesNumber = keepLastMessagesNumber,
        summaryThreshold = summaryThreshold
    )
}
