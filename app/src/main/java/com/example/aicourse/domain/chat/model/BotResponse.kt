package com.example.aicourse.domain.chat.model

/**
 * Базовый sealed interface для всех типов ответов от бота
 * Все ответы содержат сырой контент, полученный от API
 */
sealed interface BotResponse {
    val rawContent: String
}
