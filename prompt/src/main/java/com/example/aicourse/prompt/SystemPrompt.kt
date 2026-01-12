package com.example.aicourse.prompt

import android.content.Context
import com.example.aicourse.prompt.utils.ResourceReader

/**
 * Базовый interface для system prompts
 * Каждый промпт определяет параметры для модели и знает, как парсить ответ
 * Каждый промпт также определяет правила своей активации (триггеры)
 *
 * @param R тип ответа, который ожидается от этого промпта
 */
interface SystemPrompt<out R : BotResponse> {
    val temperature: Float
    val topP: Float
    val maxTokens: Int
    val modelType: ModelType?
        get() = null // По умолчанию используется модель провайдера по умолчанию

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

    /**
     * Обрабатывает входящее сообщение локально (без обращения к API)
     * Используется для промптов, которым нужна внутренняя логика обработки
     * (например, переключение режимов, показ справки и т.д.)
     *
     * @param message текст сообщения от пользователя
     * @return локальный ответ если сообщение обработано, null если нужно отправить к API
     */
    fun handleMessageLocally(message: String): R? = null

    fun extractSystemPrompt(context: Context): String? {
        return when (this) {
            is StaticSystemPrompt<*> -> contentResourceId?.let { ResourceReader.readRawResource(context, it) }
            is DynamicSystemPrompt<*> -> loadSystemPrompt(context)
            else -> null
        }
    }
}
