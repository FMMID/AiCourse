package com.example.aicourse.domain.chat.model

/**
 * Ответ от чат API с метаданными о токенах
 * Используется для всех провайдеров (GigaChat, HuggingFace, и т.д.)
 *
 * @property content текст ответа от модели
 * @property promptTokens количество токенов в запросе (null если не предоставлено API)
 * @property completionTokens количество токенов в ответе (null если не предоставлено API)
 * @property totalTokens общее количество токенов (null если не предоставлено API)
 */
data class ChatResponse(
    val content: String,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
)
