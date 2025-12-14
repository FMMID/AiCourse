package com.example.aicourse.domain.chat.model

import kotlinx.serialization.Serializable

/**
 * Типы моделей для динамического переключения
 * Каждый провайдер (GigaChat, HuggingFace) резолвит эти типы в свои конкретные модели
 */
@Serializable
sealed class ModelType {
    /**
     * Быстрая модель - минимальная латентность, базовые возможности
     */
    @Serializable
    data object Fast : ModelType()

    /**
     * Сбалансированная модель - компромисс между скоростью и качеством
     */
    @Serializable
    data object Balanced : ModelType()

    /**
     * Мощная модель - максимальное качество, может быть медленнее
     */
    @Serializable
    data object Powerful : ModelType();

    companion object {
        /**
         * Парсит строку в ModelType
         * @param value строковое представление (например, "fast", "BALANCED", "powerful")
         * @return ModelType или null если не распознано
         */
        fun fromString(value: String): ModelType? {
            return when (value.trim().uppercase()) {
                "FAST" -> Fast
                "BALANCED" -> Balanced
                "POWERFUL" -> Powerful
                else -> null
            }
        }

        /**
         * Возвращает человеко-читаемое имя типа модели
         */
        fun displayName(modelType: ModelType): String = when (modelType) {
            Fast -> "Fast"
            Balanced -> "Balanced"
            Powerful -> "Powerful"
        }
    }
}
