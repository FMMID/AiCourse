package com.example.aicourse.domain.chat.promt.dynamicTemperature

import android.util.Log
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.SystemPrompt

/**
 * Динамический системный промпт с управлением температурой
 *
 * Позволяет пользователю изменять параметр temperature для AI-ответов на лету
 * Температура влияет на креативность/случайность ответов AI
 *
 * Особенности:
 * - Активируется по триггеру /dynamic_temp
 * - Поддерживает команду /temp [значение] [сообщение] для изменения температуры
 * - Диапазон температуры: 0-2 (по умолчанию 0)
 * - НЕ передает историю сообщений в запрос к API
 *
 * @param currentSystemPrompt текущий активный промпт (для сохранения состояния)
 */
class DynamicTemperaturePrompt(
    private val currentSystemPrompt: SystemPrompt<*>
) : SystemPrompt<DynamicTemperatureResponse> {

    override val temperature: Float
        get() = currentTemperature

    override val topP: Float = 0.9f
    override val maxTokens: Int = 2048
    override var contextSummary: String? = null

    /**
     * Текущее значение температуры
     * Сохраняется между вызовами, если уже был активирован DynamicTemperaturePrompt
     */
    private var currentTemperature: Float = (currentSystemPrompt as? DynamicTemperaturePrompt)?.temperature ?: DEFAULT_TEMPERATURE

    /**
     * ID ресурса с пустым системным промптом (пользователь заполнит сам)
     */
    override val contentResourceId: Int = R.raw.dynamic_temperature_prompt

    companion object {
        private const val ACTIVATION_TRIGGER = "/dynamic_temp"
        private const val TEMP_COMMAND_PREFIX = "/temp"
        private const val DEFAULT_TEMPERATURE = 0f
        private const val MIN_TEMPERATURE = 0f
        private const val MAX_TEMPERATURE = 2f

        /**
         * Regex для извлечения команды /temp [значение] [сообщение]
         * Группы: 1 - значение температуры, 2 - остальное сообщение (опционально)
         * Формат температуры: целое число или float (например: 0, 1, 0.5, 1.25)
         * Поддерживает формат с слэшем (/temp) и без (temp)
         */
        private val TEMP_COMMAND_REGEX = Regex("""^/?temp\s+(\d+(?:\.\d+)?)(?:\s+(.+))?$""", RegexOption.IGNORE_CASE)
    }

    /**
     * Проверяет, должен ли DynamicTemperaturePrompt активироваться
     *
     * Логика:
     * 1. Если currentSystemPrompt is DynamicTemperaturePrompt (уже в режиме):
     *    - Возвращаем true (продолжаем работу в режиме)
     *
     * 2. Если currentSystemPrompt !is DynamicTemperaturePrompt (не в режиме):
     *    - Если /dynamic_temp -> активация с приветствием
     *    - Если /temp [значение] -> активация с установкой температуры
     *    - Иначе -> возвращаем false
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        return if (currentSystemPrompt is DynamicTemperaturePrompt) {
            true
        } else {
            lowerMessage == ACTIVATION_TRIGGER
                    || lowerMessage.startsWith("$ACTIVATION_TRIGGER ")
                    || lowerMessage == TEMP_COMMAND_PREFIX
                    || lowerMessage.startsWith("$TEMP_COMMAND_PREFIX ")
        }
    }

    /**
     * Парсит ответ от модели в типизированный ответ
     * В режиме DynamicTemperaturePrompt всегда возвращаем обычный текст
     */
    override fun parseResponse(rawResponse: String): DynamicTemperatureResponse {
        return DynamicTemperatureResponse(rawContent = rawResponse)
    }

    /**
     * Обрабатывает входящее сообщение локально
     *
     * Логика:
     * - Если это первая активация через /dynamic_temp (без команды /temp):
     *   Возвращаем приветственное сообщение
     *
     * - Если это первая активация через /temp [значение] [сообщение]:
     *   Устанавливаем температуру, отправляем к API (без приветствия)
     *
     * - Если уже в режиме и команда /temp без сообщения:
     *   Возвращаем подтверждение установки температуры
     *
     * - Иначе:
     *   Возвращаем null - отправляем сообщение к API
     */
    override fun handleMessageLocally(message: String): DynamicTemperatureResponse? {
        val tempCommand = parseTempCommand(message)

        if (currentSystemPrompt !is DynamicTemperaturePrompt) {
            if (tempCommand != null) {
                currentTemperature = tempCommand.temperature
                return if (tempCommand.restMessage.isNullOrBlank()) {
                    parseResponse("Температура установлена на ${tempCommand.temperature}")
                } else {
                    null
                }
            }
            return parseResponse(getWelcomeMessage())
        }

        if (tempCommand != null) {
            currentTemperature = tempCommand.temperature

            if (tempCommand.restMessage.isNullOrBlank()) {
                return parseResponse("Температура установлена на ${tempCommand.temperature}")
            }
        }

        return null
    }

    /**
     * Извлекает температуру из сообщения и возвращает очищенное сообщение
     * Используется UseCase для предобработки сообщения перед отправкой к API
     *
     * @param message исходное сообщение пользователя
     * @return пара (очищенное сообщение, была ли обновлена температура)
     */
    fun extractAndCleanMessage(message: String): String {
        val tempCommand = parseTempCommand(message)
        return if (tempCommand != null) {
            currentTemperature = tempCommand.temperature
            tempCommand.restMessage ?: message
        } else {
            message
        }
    }

    /**
     * Результат парсинга команды /temp
     * @param temperature валидированное значение температуры
     * @param restMessage остальное сообщение после команды (может быть null)
     */
    private data class TempCommandResult(
        val temperature: Float,
        val restMessage: String?
    )

    /**
     * Парсит команду /temp и извлекает температуру и остальное сообщение
     * @param message сообщение пользователя
     * @return результат парсинга или null если команда не найдена
     */
    private fun parseTempCommand(message: String): TempCommandResult? {
        val trimmed = message.trim()
        val matchResult = TEMP_COMMAND_REGEX.find(trimmed) ?: return null

        val temperatureStr = matchResult.groupValues[1]
        val restMessage = matchResult.groupValues.getOrNull(2)?.trim()

        Log.d("fed", "parseTempCommand:${matchResult.groupValues}, restMessage:${restMessage}")

        val temperature = parseAndValidateTemperature(temperatureStr)

        return TempCommandResult(temperature, restMessage)
    }

    /**
     * Парсит и валидирует значение температуры
     * Если вне диапазона - использует ближайшее допустимое значение
     */
    private fun parseAndValidateTemperature(temperatureStr: String): Float {
        val parsed = temperatureStr.toFloatOrNull() ?: DEFAULT_TEMPERATURE

        return when {
            parsed < MIN_TEMPERATURE -> MIN_TEMPERATURE
            parsed > MAX_TEMPERATURE -> MAX_TEMPERATURE
            else -> parsed
        }
    }

    /**
     * Генерирует приветственное сообщение при активации режима
     */
    private fun getWelcomeMessage(): String {
        return """
        DynamicTemperaturePrompt активирован.

        Чтобы изменить температуру, используйте команду вида:
        /temp [значение] [ваше сообщение]

        Диапазон температуры: 0-2 (по умолчанию 0)

        Примеры:
        • /temp 1.5 Расскажи шутку
        • /temp 0 Объясни квантовую физику
        • /temp 1.5 (установить температуру без запроса)
        """.trimIndent()
    }
}
