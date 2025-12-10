package com.example.aicourse.data.chat.remote.model

/**
 * Ответ от чат API с метаданными о токенах
 * Используется для всех провайдеров (GigaChat, HuggingFace, и т.д.)
 *
 * @property content текст ответа от модели
 * @property promptTokens количество токенов в запросе (null если не предоставлено API)
 * @property completionTokens количество токенов в ответе (null если не предоставлено API)
 * @property totalTokens общее количество токенов (null если не предоставлено API)
 * @property modelName имя использованной модели (null если не установлено, напр. использовалась модель по умолчанию)
 */
class ChatResponseData(
    val content: String,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null,
    val modelName: String? = null
)