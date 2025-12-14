package com.example.aicourse.domain.tools.tokenComparePrevious.model

import com.example.aicourse.domain.tools.ToolResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Разница в использовании токенов между текущим и предыдущим сообщением
 * @property promptTokensDiff изменение количества токенов запроса
 * @property completionTokensDiff изменение количества токенов ответа
 * @property totalTokensDiff изменение общего количества токенов
 */
@Serializable
@SerialName("token_usage_diff")
data class TokenUsageDiff(
    val promptTokensDiff: Int = 0,
    val completionTokensDiff: Int = 0,
    val totalTokensDiff: Int = 0
) : ToolResult {
    /**
     * Проверяет, есть ли вообще изменения (не первое сообщение)
     */
    fun hasChanges(): Boolean =
        promptTokensDiff != 0 || completionTokensDiff != 0 || totalTokensDiff != 0
}