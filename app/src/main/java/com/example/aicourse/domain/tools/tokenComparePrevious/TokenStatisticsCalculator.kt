package com.example.aicourse.domain.tools.tokenComparePrevious

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.tokenComparePrevious.model.TokenUsageDiff

/**
 * Утилита для вычисления статистики использования токенов
 */
object TokenStatisticsCalculator {

    /**
     * Вычисляет diff между текущим и предыдущим сообщением
     * @param current текущее использование токенов
     * @param previous предыдущее сообщение
     * @return объект с разницей
     */
    fun calculateDiff(
        current: Message?,
        previous: Message?
    ): TokenUsageDiff {
        if (current == null || current.tokenUsage?.hasData() == false) {
            return TokenUsageDiff()
        }

        val previousUsage = previous?.tokenUsage

        return TokenUsageDiff(
            promptTokensDiff = (current.tokenUsage?.promptTokens ?: 0) - (previousUsage?.promptTokens ?: 0),
            completionTokensDiff = (current.tokenUsage?.completionTokens ?: 0) - (previousUsage?.completionTokens ?: 0),
            totalTokensDiff = (current.tokenUsage?.totalTokens ?: 0) - (previousUsage?.totalTokens ?: 0)
        )
    }

    /**
     * Форматирует число с diff: "240 (+120)" или "100 (-50)"
     */
    fun formatWithDiff(value: Int, diff: Int, showDiff: Boolean = true): String {
        if (!showDiff || diff == 0) return value.toString()

        val sign = if (diff > 0) "+" else ""
        return "$value ($sign$diff)"
    }

    /**
     * Форматирует процент использования: "8.4%"
     */
    fun formatPercentage(used: Int, limit: Int): String {
        if (limit <= 0) return "0.0%"
        val percentage = (used.toFloat() / limit) * 100f
        return String.format("%.1f%%", percentage)
    }
}