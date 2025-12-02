package com.example.aicourse.domain.chat.model

/**
 * Базовый sealed interface для system prompts
 * Каждый промпт определяет параметры для модели и знает, как парсить ответ
 *
 * @param R тип ответа, который ожидается от этого промпта
 */
sealed interface SystemPrompt<out R : BotResponse> {
    val temperature: Float
    val topP: Float
    val content: String?

    /**
     * Парсит сырой ответ от модели в типизированный ответ
     * @param rawResponse сырой текст ответа от модели
     * @return типизированный ответ
     */
    fun parseResponse(rawResponse: String): R
}
