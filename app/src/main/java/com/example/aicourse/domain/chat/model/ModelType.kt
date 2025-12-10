package com.example.aicourse.domain.chat.model

/**
 * Типы моделей для динамического переключения
 * Каждый провайдер (GigaChat, HuggingFace) резолвит эти типы в свои конкретные модели
 */
sealed class ModelType {
    /**
     * Быстрая модель - минимальная латентность, базовые возможности
     */
    data object FAST : ModelType()

    /**
     * Сбалансированная модель - компромисс между скоростью и качеством
     */
    data object BALANCED : ModelType()

    /**
     * Мощная модель - максимальное качество, может быть медленнее
     */
    data object POWERFUL : ModelType();

    companion object {
        /**
         * Парсит строку в ModelType
         * @param value строковое представление (например, "fast", "BALANCED", "powerful")
         * @return ModelType или null если не распознано
         */
        fun fromString(value: String): ModelType? {
            return when (value.trim().uppercase()) {
                "FAST" -> FAST
                "BALANCED" -> BALANCED
                "POWERFUL" -> POWERFUL
                else -> null
            }
        }

        /**
         * Возвращает человеко-читаемое имя типа модели
         */
        fun displayName(modelType: ModelType): String = when (modelType) {
            FAST -> "Fast"
            BALANCED -> "Balanced"
            POWERFUL -> "Powerful"
        }
    }
}
