package com.example.aicourse.domain.chat.promt.pc

import com.example.aicourse.domain.chat.promt.BotResponse

/**
 * Ответ ассистента по сборке ПК
 * Соответствует API формату из системного промпта
 *
 * @param rawContent исходный текст ответа от API
 * @param isFinished true если сборка готова, false если идет диалог
 * @param question текст вопроса от ассистента (null если isFinished = true)
 * @param pcBuild финальная конфигурация ПК (null если isFinished = false)
 */
data class PcBuildResponse(
    override val rawContent: String,
    val isFinished: Boolean,
    val question: String? = null,
    val pcBuild: PcBuild? = null
) : BotResponse {
    /**
     * Проверяет, является ли ответ финальной сборкой
     */
    val isFinalBuild: Boolean
        get() = isFinished && pcBuild != null
}
