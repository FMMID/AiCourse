package com.example.aicourse.domain.chat.promt.dynamicTemperature

import android.util.Log
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.StaticSystemPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 * @param currentTemperature текущее значение температуры (сохраняется между вызовами)
 */
@Serializable
@SerialName("dynamic_temperature")
data class DynamicTemperaturePrompt(
    private var currentTemperature: Float = DEFAULT_TEMPERATURE
) : StaticSystemPrompt<DynamicTemperatureResponse> {

    override val temperature: Float
        get() = currentTemperature

    override val topP: Float = 0.9f
    override val maxTokens: Int = 2048

    /**
     * ID ресурса с пустым системным промптом (пользователь заполнит сам)
     */
    override val contentResourceId: Int = R.raw.dynamic_temperature_prompt

    companion object {
        private const val ACTIVATION_TRIGGER = "/dynamic_temp"
        private const val TEMP_COMMAND_PREFIX = "/temp"
        const val DEFAULT_TEMPERATURE = 0f
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
     * Логика (после рефакторинга):
     * - Если /dynamic_temp -> активация с приветствием
     * - Если /temp [значение] -> активация с установкой температуры
     * - Иначе -> возвращаем false
     *
     * Примечание: проверка "уже в режиме" теперь выполняется в SimpleChatStrategy
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        return lowerMessage == ACTIVATION_TRIGGER
                || lowerMessage.startsWith("$ACTIVATION_TRIGGER ")
                || lowerMessage == TEMP_COMMAND_PREFIX
                || lowerMessage.startsWith("$TEMP_COMMAND_PREFIX ")
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
     * Логика (после рефакторинга):
     * - Если команда /temp [значение] без сообщения:
     *   Устанавливаем температуру, возвращаем подтверждение
     *
     * - Если команда /temp [значение] [сообщение]:
     *   Устанавливаем температуру, отправляем к API (возвращаем null)
     *
     * - Если это активация /dynamic_temp:
     *   Возвращаем приветственное сообщение
     *
     * - Иначе:
     *   Возвращаем null - отправляем сообщение к API
     */
    override fun handleMessageLocally(message: String): DynamicTemperatureResponse? {
        val tempCommand = parseTempCommand(message)

        if (tempCommand != null) {
            currentTemperature = tempCommand.temperature
            return if (tempCommand.restMessage.isNullOrBlank()) {
                parseResponse("Температура установлена на ${tempCommand.temperature}")
            } else {
                null
            }
        }

        // Если это активационный триггер - показываем welcome message
        val lowerMessage = message.trim().lowercase()
        if (lowerMessage == ACTIVATION_TRIGGER || lowerMessage.startsWith("$ACTIVATION_TRIGGER ")) {
            return parseResponse(getWelcomeMessage())
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
