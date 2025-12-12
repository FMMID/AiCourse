package com.example.aicourse.domain.tools.context.model

/**
 * Детальная информация о распределении токенов в контекстном окне
 *
 * Разделяет общее использование контекста на три компонента для более точного мониторинга
 * и диагностики использования токенов.
 *
 * @property sizeOfSummaryMessages количество токенов в суммаризированной части истории
 * @property sizeOfActiveMessages количество токенов в активных (несуммаризированных) сообщениях
 * @property sizeOfSystemPrompt количество токенов в системном промпте
 */
data class ContextInfo(
    val sizeOfSummaryMessages: Int,
    val sizeOfActiveMessages: Int,
    val sizeOfSystemPrompt: Int
)