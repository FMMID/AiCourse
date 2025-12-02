package com.example.aicourse.domain.chat.model

/**
 * Базовый sealed interface для system prompts
 * Каждый промпт определяет параметры для модели и знает, как парсить ответ
 * Каждый промпт также определяет правила своей активации (триггеры)
 *
 * @param R тип ответа, который ожидается от этого промпта
 */
sealed interface SystemPrompt<out R : BotResponse> {
    val temperature: Float
    val topP: Float
    val content: String?

    /**
     * Проверяет, должен ли этот промпт активироваться для данного сообщения
     * @param message текст сообщения от пользователя
     * @return true, если промпт подходит для этого сообщения
     */
    fun matches(message: String): Boolean

    /**
     * Парсит сырой ответ от модели в типизированный ответ
     * @param rawResponse сырой текст ответа от модели
     * @return типизированный ответ
     */
    fun parseResponse(rawResponse: String): R
}
