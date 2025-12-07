package com.example.aicourse.domain.chat.model.dynamic

/**
 * Конфигурация внутреннего промпта для DynamicSystemPrompt
 *
 * @param id уникальный идентификатор промпта (например, "python", "english")
 * @param name отображаемое имя для UI (например, "Python Expert", "English Teacher")
 * @param triggers список триггеров для активации этого промпта (например, ["/python", "python mode"])
 * @param contentResourceId ID ресурса из res/raw для system content
 */
data class InternalPromptConfig(
    val id: String,
    val name: String,
    val triggers: List<String>,
    val contentResourceId: Int
)
