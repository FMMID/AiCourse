package com.example.aicourse.domain.chat.model.dynamicModel

import android.util.Log
import com.example.aicourse.R
import com.example.aicourse.domain.chat.model.ModelType
import com.example.aicourse.domain.chat.model.SystemPrompt

/**
 * Динамический системный промпт с управлением типом модели
 *
 * Позволяет пользователю переключаться между разными типами моделей на лету
 * Каждый провайдер (GigaChat, HuggingFace) резолвит типы в свои конкретные модели
 *
 * Особенности:
 * - Активируется по триггеру /dynamic_model
 * - Поддерживает команду /model [TYPE] [сообщение] для смены типа модели
 * - Доступные типы: FAST, BALANCED, POWERFUL
 *
 * @param currentSystemPrompt текущий активный промпт (для сохранения состояния)
 */
class DynamicModelPrompt(
    private val currentSystemPrompt: SystemPrompt<*>
) : SystemPrompt<DynamicModelResponse> {

    override val temperature: Float = 0.7f
    override val topP: Float = 0.9f
    override val maxTokens: Int = 2048
    override val contentResourceId: Int = R.raw.dynamic_model_prompt

    override val modelType: ModelType?
        get() = currentModelType

    /**
     * Текущий тип модели
     * Сохраняется между вызовами, если уже был активирован DynamicModelPrompt
     */
    private var currentModelType: ModelType? = (currentSystemPrompt as? DynamicModelPrompt)?.modelType

    companion object {
        private const val ACTIVATION_TRIGGER = "/dynamic_model"
        private const val MODEL_COMMAND_PREFIX = "/model"

        /**
         * Regex для извлечения команды /model [TYPE] [сообщение]
         * Группы: 1 - тип модели (FAST/BALANCED/POWERFUL), 2 - остальное сообщение (опционально)
         * Поддерживает формат с слэшем (/model) и без (model)
         */
        private val MODEL_COMMAND_REGEX = Regex("""^/?model\s+(\w+)(?:\s+(.+))?$""", RegexOption.IGNORE_CASE)
    }

    /**
     * Проверяет, должен ли DynamicModelPrompt активироваться
     *
     * Логика:
     * 1. Если currentSystemPrompt is DynamicModelPrompt (уже в режиме):
     *    - Возвращаем true (продолжаем работу в режиме)
     *
     * 2. Если currentSystemPrompt !is DynamicModelPrompt (не в режиме):
     *    - Если /dynamic_model -> активация с приветствием
     *    - Если /model [TYPE] -> активация с установкой модели
     *    - Иначе -> возвращаем false
     */
    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        return if (currentSystemPrompt is DynamicModelPrompt) {
            true
        } else {
            lowerMessage == ACTIVATION_TRIGGER
                    || lowerMessage.startsWith("$ACTIVATION_TRIGGER ")
                    || lowerMessage == MODEL_COMMAND_PREFIX
                    || lowerMessage.startsWith("$MODEL_COMMAND_PREFIX ")
        }
    }

    /**
     * Парсит ответ от модели в типизированный ответ
     * В режиме DynamicModelPrompt всегда возвращаем обычный текст
     */
    override fun parseResponse(rawResponse: String): DynamicModelResponse {
        return DynamicModelResponse(rawContent = rawResponse)
    }

    /**
     * Обрабатывает входящее сообщение локально
     *
     * Логика:
     * - Если это первая активация через /dynamic_model (без команды /model):
     *   Возвращаем приветственное сообщение
     *
     * - Если это первая активация через /model [TYPE] [сообщение]:
     *   Устанавливаем тип модели, отправляем к API (без приветствия)
     *
     * - Если уже в режиме и команда /model без сообщения:
     *   Возвращаем подтверждение установки типа модели
     *
     * - Иначе:
     *   Возвращаем null - отправляем сообщение к API
     */
    override fun handleMessageLocally(message: String): DynamicModelResponse? {
        val modelCommand = parseModelCommand(message)

        if (currentSystemPrompt !is DynamicModelPrompt) {
            if (modelCommand != null) {
                currentModelType = modelCommand.modelType
                return if (modelCommand.restMessage.isNullOrBlank()) {
                    parseResponse("Тип модели установлен: ${ModelType.displayName(modelCommand.modelType)}")
                } else {
                    null
                }
            }
            return parseResponse(getWelcomeMessage())
        }

        if (modelCommand != null) {
            currentModelType = modelCommand.modelType

            if (modelCommand.restMessage.isNullOrBlank()) {
                return parseResponse("Тип модели установлен: ${ModelType.displayName(modelCommand.modelType)}")
            }
        }

        return null
    }

    /**
     * Извлекает тип модели из сообщения и возвращает очищенное сообщение
     * Используется UseCase для предобработки сообщения перед отправкой к API
     *
     * @param message исходное сообщение пользователя
     * @return очищенное сообщение (без команды /model)
     */
    fun extractAndCleanMessage(message: String): String {
        val modelCommand = parseModelCommand(message)
        return if (modelCommand != null) {
            currentModelType = modelCommand.modelType
            modelCommand.restMessage ?: message
        } else {
            message
        }
    }

    /**
     * Результат парсинга команды /model
     * @param modelType валидированный тип модели
     * @param restMessage остальное сообщение после команды (может быть null)
     */
    private data class ModelCommandResult(
        val modelType: ModelType,
        val restMessage: String?
    )

    /**
     * Парсит команду /model и извлекает тип модели и остальное сообщение
     * @param message сообщение пользователя
     * @return результат парсинга или null если команда не найдена
     */
    private fun parseModelCommand(message: String): ModelCommandResult? {
        val trimmed = message.trim()
        val matchResult = MODEL_COMMAND_REGEX.find(trimmed) ?: return null

        val modelTypeStr = matchResult.groupValues[1]
        val restMessage = matchResult.groupValues.getOrNull(2)?.trim()

        Log.d("DynamicModelPrompt", "parseModelCommand: ${matchResult.groupValues}, restMessage: $restMessage")

        val modelType = ModelType.fromString(modelTypeStr) ?: return null

        return ModelCommandResult(modelType, restMessage)
    }

    /**
     * Генерирует приветственное сообщение при активации режима
     */
    private fun getWelcomeMessage(): String {
        return """
        DynamicModelPrompt активирован.

        Чтобы изменить тип модели, используйте команду вида:
        /model [TYPE] [ваше сообщение]

        Доступные типы моделей:
        • FAST - быстрая модель (минимальная латентность)
        • BALANCED - сбалансированная модель (компромисс)
        • POWERFUL - мощная модель (максимальное качество)

        Примеры:
        • /model FAST Расскажи шутку
        • /model POWERFUL Объясни квантовую физику
        • /model BALANCED (установить тип без запроса)

        Текущий тип: ${currentModelType?.let { ModelType.displayName(it) } ?: "по умолчанию"}
        """.trimIndent()
    }
}
