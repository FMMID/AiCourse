package com.example.aicourse.domain.chat.promt.dynamicSystemPrompt

import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.SystemPrompt

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
 * @param availablePrompts список доступных внутренних промптов
 */
// TODO: Класс сделан для удоства выполнения задания, так переключение разного SystemPrompt можно сделать
//  переключением разных имплементаций SystemPrompt, что делает эту релизацию бесполезной
class DynamicSystemPrompt(
    private val currentSystemPrompt: SystemPrompt<*>
) : SystemPrompt<DynamicSystemPromptResponse> {

    /**
     * Текущий активный внутренний промпт
     * null означает, что пользователь только активировал DynamicSystemPrompt
     * и ему нужно показать список доступных промптов
     */
    var activeInternalPrompt: InternalPromptConfig? = (currentSystemPrompt as? DynamicSystemPrompt)?.activeInternalPrompt
        private set

    override val temperature: Float = 0.7f
    override val topP: Float = 0.9f
    override val maxTokens: Int = 2048
    override var contextSummary: String? = null

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

    /**
     * Возвращает contentResourceId активного внутреннего промпта
     * Если промпт не выбран, возвращает null (обычный режим без system content)
     */
    override val contentResourceId: Int?
        get() = activeInternalPrompt?.contentResourceId

    companion object {
        private val ACTIVATION_TRIGGERS = listOf("/dynamic", "/expert")
    }

    /**
     * Проверяет, должен ли DynamicSystemPrompt активироваться
     *
     * Логика:
     * 1. Если currentSystemPrompt is DynamicSystemPrompt (уже в динамическом режиме):
     *    - Если /exit -> возвращаем false (выход из режима)
     *    - Если триггер внутреннего промпта -> переключаемся и возвращаем true
     *    - Иначе (обычное сообщение) -> возвращаем true (продолжаем работу)
     *
     * 2. Если currentSystemPrompt !is DynamicSystemPrompt (не в динамическом режиме):
     *    - Если ACTIVATION_TRIGGERS (/dynamic, /expert) -> возвращаем true (активация)
     *    - Проверка: триггер должен быть либо точным совпадением, либо с пробелом после
     *    - Это предотвращает срабатывание на /dynamic_temp и подобные команды
     *    - Иначе -> возвращаем false
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        return if (currentSystemPrompt is DynamicSystemPrompt) {
            when {
                lowerMessage == "/exit" -> false
                else -> {
                    switchToInternalPrompt(message)
                    true
                }
            }
        } else {
            ACTIVATION_TRIGGERS.any { trigger ->
                lowerMessage == trigger || lowerMessage.startsWith("$trigger ")
            }
        }
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
     * - Если activeInternalPrompt == null (внутренний промпт не выбран):
     *   Всегда возвращаем приветственное сообщение с меню
     *
     * - Если activeInternalPrompt != null (уже выбран внутренний промпт):
     *   Возвращаем null - отправляем сообщение к API с текущим system content
     */
    override fun handleMessageLocally(message: String): DynamicSystemPromptResponse? {
        return if (activeInternalPrompt == null) {
            parseResponse(getWelcomeMessage())
        } else {
            null
        }
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
