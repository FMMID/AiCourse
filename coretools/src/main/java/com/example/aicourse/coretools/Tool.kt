package com.example.aicourse.coretools

/**
 * Базовый интерфейс инструмента. Каждая конкретная функция (поиск, погода, заметки)
 * должна реализовывать этот интерфейс.
 */
interface Tool {
    /** Уникальное имя инструмента, которое передается LLM (например, "search_documents") */
    val name: String

    /** Описание функционала для LLM. Чем точнее описание, тем лучше ИИ поймет, когда вызывать тул */
    val description: String

    /** * Схема входных параметров в формате JSON Schema.
     * Помогает LLM сформировать корректный JSON для вызова.
     */
    val parameters: Map<String, Any>

    /** Категория инструмента */
    val type: ToolType

    /**
     * Основная логика выполнения.
     * @param args Аргументы, подготовленные LLM (распарсенный JSON)
     * @return [ToolResult] с результатом выполнения или описанием ошибки
     */
    suspend fun execute(args: Map<String, Any>): ToolResult
}
