package com.example.aicourse.domain.tools.context.model

/**
 * Конфигурация контекстного окна для управления историей сообщений
 *
 * @property originalLimit максимальное количество токенов в контексте
 * @property summaryThreshold порог (в процентах от лимита), при котором следует выполнить суммаризацию
 */
data class ContextWindow(
    val originalLimit: Int,
    val summaryThreshold: Float = 0.8f
) {

    /**
     * Проверяет, необходима ли суммаризация диалога
     *
     * @param historyTokens текущее количество токенов в истории
     * @return true если история превышает порог суммаризации
     */
    fun shouldSummarizeDialog(historyTokens: Int): Boolean {
        return historyTokens > originalLimit * summaryThreshold
    }
}