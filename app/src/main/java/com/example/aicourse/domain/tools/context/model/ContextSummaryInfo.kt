package com.example.aicourse.domain.tools.context.model

/**
 * Value object содержащий информацию о суммаризированном контексте
 *
 * @property message суммаризированный текст диалога
 * @property totalTokens оценочное количество токенов в суммаризации
 */
data class ContextSummaryInfo(
    val message: String,
    val totalTokens: Int
)