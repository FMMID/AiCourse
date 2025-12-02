package com.example.aicourse.domain.chat.model

/**
 * Базовый interface для всех типов ответов от бота
 * Все ответы содержат сырой контент, полученный от API
 */
interface BotResponse {
    val rawContent: String
}
