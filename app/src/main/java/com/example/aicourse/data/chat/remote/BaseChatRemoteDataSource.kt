package com.example.aicourse.data.chat.remote

import android.util.Log
import com.example.aicourse.data.tools.context.SummarizeContextDataSource
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Базовый класс для удаленных источников данных чата
 * Содержит общую логику для всех Chat API провайдеров
 */
abstract class BaseChatRemoteDataSource : ChatRemoteDataSource, SummarizeContextDataSource {

    companion object {
        /**
         * Системный промпт для суммаризации контекста
         * Используется всеми провайдерами для единообразия результатов
         */
        const val SUMMARIZATION_SYSTEM_PROMPT = """
                Ниже приведен отрывок диалога. Твоя задача: сжато пересказать его,
                сохранив все ключевые факты, имена, цифры и договоренности.
                Игнорируй приветствия и эмоции.
                Отвечай только пересказом, без дополнительных комментариев.
            """

        /**
         * Параметры для суммаризации
         * Низкая температура и topP для фактической, детерминированной генерации
         */
        const val SUMMARIZATION_TEMPERATURE = 0.3
        const val SUMMARIZATION_TOP_P = 0.1
        const val SUMMARIZATION_MAX_TOKENS = 1024
    }

    /**
     * TAG для логирования (переопределяется в наследниках)
     */
    protected abstract val logTag: String

    /**
     * Настроенный HTTP клиент с поддержкой JSON сериализации и логирования
     */
    protected val httpClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(logTag, message)
                }
            }
            level = LogLevel.HEADERS
        }
    }

    /**
     * Суммаризирует историю сообщений используя AI модель
     * Использует единый системный промпт и параметры для всех провайдеров
     *
     * @param messageHistory отформатированная история сообщений для суммаризации
     * @return суммаризированный текст
     */
    override suspend fun summarizeContext(messageHistory: List<Message>, existContextSummary: ContextSummaryInfo?): ContextSummaryInfo =
        withContext(Dispatchers.IO) {
            try {
                val systemPrompt = if (existContextSummary != null) {
                    SUMMARIZATION_SYSTEM_PROMPT.trimIndent().plus("\nПрошлая выжимка диалога:\n").plus(existContextSummary.message)
                } else {
                    SUMMARIZATION_SYSTEM_PROMPT.trimIndent()
                }

                val summary = sendSummarizationRequest(
                    systemPrompt = systemPrompt,
                    messageHistory = messageHistory,
                    temperature = SUMMARIZATION_TEMPERATURE,
                    topP = SUMMARIZATION_TOP_P,
                    maxTokens = SUMMARIZATION_MAX_TOKENS
                )

                Log.d(logTag, "Context summarized successfully")
                return@withContext summary

            } catch (e: Exception) {
                Log.e(logTag, "Error summarizing context", e)
                throw Exception("Ошибка суммаризации контекста: ${e.message}", e)
            }
        }

    /**
     * Отправляет запрос на суммаризацию к конкретному провайдеру
     * Каждый провайдер реализует этот метод используя свой формат API
     *
     * @param systemPrompt системный промпт для суммаризации
     * @param messageHistory история сообщений для суммаризации
     * @param temperature параметр температуры модели
     * @param topP параметр top-p модели
     * @param maxTokens максимальное количество токенов в ответе
     * @return суммаризированный текст
     */
    protected abstract suspend fun sendSummarizationRequest(
        systemPrompt: String,
        messageHistory: List<Message>,
        temperature: Double,
        topP: Double,
        maxTokens: Int
    ): ContextSummaryInfo

    /**
     * Фабричный метод для создания сообщения конкретного провайдера
     *
     * @param T тип сообщения провайдера (ChatMessage, HfChatMessage, и т.д.)
     * @param role роль сообщения (system, user, assistant)
     * @param content содержимое сообщения
     * @return объект сообщения провайдера
     */
    protected abstract fun <T> createMessage(role: String, content: String): T

    /**
     * Строит список сообщений для отправки в API
     * Общая логика для всех провайдеров: system prompt + история + текущее сообщение
     *
     * @param T тип сообщения провайдера
     * @param systemContent системный промпт (может быть null)
     * @param messageHistory история предыдущих сообщений
     * @param roleSystem константа для роли "system"
     * @param messageTypeToRole функция конвертации MessageType в роль провайдера
     * @return список сообщений для API запроса
     */
    protected fun <T> buildMessagesList(
        systemContent: String?,
        messageHistory: List<Message>,
        roleSystem: String,
        messageTypeToRole: (MessageType) -> String
    ): List<T> {

        return buildList {
            // Добавляем system prompt если есть
            systemContent?.let {
                add(createMessage(roleSystem, it))
            }

            // Добавляем историю сообщений
            messageHistory.forEach { msg ->
                add(createMessage(messageTypeToRole(msg.type), msg.text))
            }
        }
    }

    /**
     * Закрывает HTTP клиент и освобождает ресурсы
     */
    open fun close() {
        httpClient.close()
    }
}
