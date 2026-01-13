package com.example.aicourse.coretools

/**
 * Результат выполнения инструмента. Используется sealed class для обеспечения
 * строгой обработки всех исходов (успех/ошибка).
 */
sealed class ToolResult {
    /** Успешное выполнение. Content передается обратно в LLM как контекст */
    data class Success(val content: String) : ToolResult()

    /** Ошибка выполнения. Message может быть передан LLM, чтобы она объяснила проблему пользователю */
    data class Error(val message: String, val cause: Throwable? = null) : ToolResult()
}