package com.example.aicourse.domain.tools.context.model

import com.example.aicourse.domain.tools.ToolResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Результат обработки контекстного окна, содержащий статистику использования
 * и индикацию необходимости суммаризации
 *
 * Этот класс агрегирует всю информацию о текущем состоянии контекста и предоставляет
 * удобные методы для UI отображения.
 *
 * @property contextInfo детальная информация о распределении токенов
 * @property totalUsedTokens общее количество использованных токенов (summary + active + system)
 * @property contextLimit максимальный лимит токенов
 * @property usagePercentage процент использования контекста (0.0 - 1.0)
 * @property wasSummarized флаг, указывающий была ли выполнена суммаризация
 * @property summarizationError сообщение об ошибке при суммаризации (null если ошибок не было)
 */
@Serializable
@SerialName("context_window_info")
data class ContextWindowInfo(
    val contextInfo: ContextInfo,
    val totalUsedTokens: Int,
    val contextLimit: Int,
    val usagePercentage: Float,
    val wasSummarized: Boolean = false,
    val summarizationError: String? = null
) : ToolResult {

    /**
     * Определяет цветовой уровень для UI индикатора на основе процента использования
     * - GREEN: < 70% использования
     * - YELLOW: 70-85% использования
     * - RED: > 85% использования
     */
    fun getIndicatorColorLevel(): ColorLevel {
        return when {
            usagePercentage < 0.70f -> ColorLevel.GREEN
            usagePercentage < 0.85f -> ColorLevel.YELLOW
            else -> ColorLevel.RED
        }
    }

    @Serializable
    enum class ColorLevel {
        GREEN, YELLOW, RED
    }
}
