package com.example.aicourse.domain.chat.promt.dynamicSystemPrompt

import android.content.Context
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.DynamicSystemPrompt
import com.example.aicourse.domain.utils.ResourceReader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Динамический системный промпт, который может переключаться между несколькими
 * внутренними промптами в зависимости от триггер-фраз пользователя
 *
 * Особенности:
 * - Активируется по триггерам /dynamic или /expert
 * - Содержит несколько внутренних промптов с разными system content
 * - Пользователь может переключаться между промптами без сброса истории
 * - Для выхода используется команда /exit
 *
 * @param activeInternalPrompt текущий активный внутренний промпт (сохраняется между вызовами)
 */
// TODO: Класс сделан для удоства выполнения задания, так переключение разного SystemPrompt можно сделать
//  переключением разных имплементаций SystemPrompt, что делает эту релизацию бесполезной
@Serializable
@SerialName("dynamic_system_prompt")
data class DynamicSystemPrompt(var activeInternalPrompt: InternalPromptConfig? = null) : DynamicSystemPrompt<DynamicSystemPromptResponse> {

    override val temperature: Float = 0.7f
    override val topP: Float = 0.9f
    override val maxTokens: Int = 2048

    private val availablePrompts: List<InternalPromptConfig> = listOf(
        InternalPromptConfig(
            id = "startaper",
            name = "Стартапер",
            triggers = listOf("/стартапер"),
            contentResourceId = R.raw.dynamic_startupper
        ),
        InternalPromptConfig(
            id = "official",
            name = "Бюрократ",
            triggers = listOf("/бюрократ"),
            contentResourceId = R.raw.dynamic_official
        )
    )

    companion object {
        private val ACTIVATION_TRIGGERS = listOf("/dynamic", "/expert")
    }

    /**
     * Возвращает contentResourceId активного внутреннего промпта
     * Если промпт не выбран, возвращает null (обычный режим без system content)
     */
    override fun loadSystemPrompt(context: Context): String? {
        return activeInternalPrompt?.contentResourceId?.let { ResourceReader.readRawResource(context, it) }
    }

    /**
     * Проверяет, должен ли DynamicSystemPrompt активироваться
     *
     * Логика (после рефакторинга):
     * - Если /dynamic или /expert -> активация с приветствием
     * - Если триггер внутреннего промпта -> активация с переключением
     * - Если /exit -> возвращаем false (выход)
     * - Иначе -> возвращаем false
     *
     * Примечание: проверка "уже в режиме" теперь выполняется в SimpleChatStrategy
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        // Проверка на выход
        if (lowerMessage == "/exit") {
            return false
        }

        // Проверка на активационные триггеры
        if (ACTIVATION_TRIGGERS.any { trigger ->
                lowerMessage == trigger || lowerMessage.startsWith("$trigger ")
            }) {
            return true
        }

        // Проверка на триггеры внутренних промптов
        val hasInternalTrigger = availablePrompts.any { config ->
            config.triggers.any { trigger ->
                lowerMessage.startsWith(trigger.lowercase())
            }
        }

        return hasInternalTrigger
    }

    /**
     * Парсит ответ от модели в типизированный ответ
     * В режиме DynamicSystemPrompt всегда возвращаем обычный текст
     */
    override fun parseResponse(rawResponse: String): DynamicSystemPromptResponse {
        return DynamicSystemPromptResponse(rawContent = rawResponse)
    }

    /**
     * Обрабатывает входящее сообщение локально
     *
     * Логика:
     * - Если триггер внутреннего промпта:
     *   Переключаемся на промпт, возвращаем подтверждение
     *
     * - Если это активация /dynamic или /expert:
     *   Возвращаем приветственное сообщение с меню
     *
     * - Если activeInternalPrompt != null (уже выбран):
     *   Возвращаем null - отправляем сообщение к API
     *
     * - Иначе:
     *   Возвращаем null
     */
    override fun handleMessageLocally(message: String): DynamicSystemPromptResponse? {
        // Проверяем триггеры переключения промпта
        if (switchToInternalPrompt(message)) {
            return parseResponse("Промпт переключен на: ${activeInternalPrompt?.name}\n\nТеперь вы можете отправить сообщение.")
        }

        // Если это активационный триггер - показываем welcome message
        val lowerMessage = message.trim().lowercase()
        if (ACTIVATION_TRIGGERS.any { trigger ->
                lowerMessage == trigger || lowerMessage.startsWith("$trigger ")
            }) {
            return parseResponse(getWelcomeMessage())
        }

        // Если промпт не выбран и это не активационная команда
        if (activeInternalPrompt == null) {
            return parseResponse(getWelcomeMessage())
        }

        // Иначе отправляем к API
        return null
    }

    /**
     * Пытается переключиться на внутренний промпт по триггеру
     * @param message сообщение пользователя
     * @return true если переключение произошло, false если триггер не найден
     */
    fun switchToInternalPrompt(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        val matchedPrompt = availablePrompts.firstOrNull { config ->
            config.triggers.any { trigger ->
                lowerMessage.startsWith(trigger.lowercase())
            }
        }

        return if (matchedPrompt != null) {
            activeInternalPrompt = matchedPrompt
            true
        } else {
            false
        }
    }

    /**
     * Сбрасывает активный промпт (при /exit или сбросе)
     */
    fun resetActivePrompt() {
        activeInternalPrompt = null
    }

    /**
     * Генерирует приветственное сообщение со списком доступных промптов
     * Используется когда пользователь активирует DynamicSystemPrompt
     */
    private fun getWelcomeMessage(): String {
        val promptsList = availablePrompts.joinToString("\n") { config ->
            "• ${config.triggers.first()} — ${config.name}"
        }

        return """
        Режим динамических промптов активирован! 🔧

        Доступные режимы:
        $promptsList
        • /exit — выход из динамического режима

        Выберите режим, отправив соответствующую команду.
        """.trimIndent()
    }

    /**
     * Возвращает имя активного промпта для UI
     */
    fun getActivePromptName(): String? = activeInternalPrompt?.name
}
