package com.example.aicourse.domain.tools.context

import android.util.Log
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.domain.tools.context.model.ContextWindow
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo

class ContextWindowManager(
    private val targetContextWindow: ContextWindow,
    private val contextRepository: ContextRepository
) : Tool<DataForReceive.Simple> {

    companion object {
        /**
         * Максимальный размер буфера сообщений
         * Предотвращает неограниченный рост буфера в памяти
         */
        private const val MAX_BUFFER_SIZE = 100
    }

    private var messageBuffer: MutableList<Message> = mutableListOf()
    private var contextSummary: ContextSummaryInfo = ContextSummaryInfo(message = "", totalTokens = 0)
    private var contextInfo: ContextInfo = ContextInfo(sizeOfSummaryMessages = 0, sizeOfActiveMessages = 0, sizeOfSystemPrompt = 0)
    private var lastSummarizationError: String? = null
    private var lastOperationWasSummarized: Boolean = false

    /**
     * Обрабатывает историю сообщений, выполняя суммаризацию при необходимости
     * @param dataForSend данные для отправки, включая историю и активный промпт
     * @return обработанная история сообщений (с возможным сжатием)
     */
    suspend fun processMessageHistory(dataForSend: DataForSend.RemoteCall): List<Message> {
        // 1. Обновляем messageBuffer с ограничением размера
        messageBuffer.clear()
        messageBuffer.addAll(dataForSend.messageHistory.takeLast(MAX_BUFFER_SIZE))

        // 2. Вычисляем размеры
        val systemPromptSize = estimateSystemPromptSize(dataForSend.activePrompt)
        val activeMessagesSize = calculateMessagesTokenSize(messageBuffer)

        // 3. Проверяем, нужна ли суммаризация
        val totalTokens = contextSummary.totalTokens + activeMessagesSize + systemPromptSize
        val needSummary = targetContextWindow.shouldSummarizeDialog(totalTokens)

        if (needSummary && messageBuffer.size > 5) {
            return performSummarization(systemPromptSize)
        }

        // Суммаризация не нужна
        lastOperationWasSummarized = false

        // Обновляем contextInfo
        contextInfo = ContextInfo(
            sizeOfSummaryMessages = contextSummary.totalTokens,
            sizeOfActiveMessages = activeMessagesSize,
            sizeOfSystemPrompt = systemPromptSize
        )

        return messageBuffer
    }

    /**
     * Выполняет суммаризацию части истории сообщений
     * Оставляет 5 последних сообщений без изменений
     */
    private suspend fun performSummarization(systemPromptSize: Int): List<Message> {
        try {
            val messagesToKeep = 5
            val messagesToSummarize = messageBuffer.dropLast(messagesToKeep)
            val messagesToRetain = messageBuffer.takeLast(messagesToKeep)

            // Вызываем API для суммаризации
            val newSummary = contextRepository.summarizeContext(messagesToSummarize)

            // Комбинируем с предыдущей суммаризацией если есть
            contextSummary = if (contextSummary.message.isNotEmpty()) {
                ContextSummaryInfo(
                    message = "ПРЕДЫДУЩИЙ КОНТЕКСТ:\n${contextSummary.message}\n\nНОВЫЕ СООБЩЕНИЯ:\n${newSummary.message}",
                    totalTokens = contextSummary.totalTokens + newSummary.totalTokens
                )
            } else {
                newSummary
            }

            // Обновляем messageBuffer
            messageBuffer.clear()
            messageBuffer.addAll(messagesToRetain)

            // Сбрасываем ошибку после успешной суммаризации
            lastSummarizationError = null

            // Обновляем contextInfo
            val activeMessagesSize = calculateMessagesTokenSize(messageBuffer)
            contextInfo = ContextInfo(
                sizeOfSummaryMessages = contextSummary.totalTokens,
                sizeOfActiveMessages = activeMessagesSize,
                sizeOfSystemPrompt = systemPromptSize
            )

            // Устанавливаем флаг успешной суммаризации
            lastOperationWasSummarized = true

            Log.d("ContextWindowManager", "Context summarized: ${messagesToSummarize.size} messages compressed")
            return messagesToRetain

        } catch (e: Exception) {
            Log.e("ContextWindowManager", "Summarization failed", e)
            // Сохраняем ошибку для информирования пользователя
            lastSummarizationError = "Ошибка суммаризации: ${e.message}"
            // Сбрасываем флаг при ошибке
            lastOperationWasSummarized = false
            // При ошибке возвращаем все сообщения без сжатия
            return messageBuffer
        }
    }

    /**
     * Обрабатывает полученное сообщение от бота и возвращает статистику контекстного окна
     * @param processData данные полученного сообщения
     * @return ContextWindowInfo с актуальной статистикой использования контекста
     */
    override fun processData(processData: DataForReceive.Simple): ToolResult {
        val botMessage = processData.message

        // Обновляем размер активных сообщений
        val messageTokens = botMessage.tokenUsage?.totalTokens
            ?: TokenEstimator.estimateTokenCount(botMessage.text)

        val updatedActiveSize = contextInfo.sizeOfActiveMessages + messageTokens
        contextInfo = contextInfo.copy(sizeOfActiveMessages = updatedActiveSize)

        // Вычисляем общее использование
        val totalUsed = contextInfo.sizeOfSummaryMessages +
                contextInfo.sizeOfActiveMessages +
                contextInfo.sizeOfSystemPrompt

        val usagePercentage = totalUsed.toFloat() / targetContextWindow.originalLimit

        return ContextWindowInfo(
            contextInfo = contextInfo,
            totalUsedTokens = totalUsed,
            contextLimit = targetContextWindow.originalLimit,
            usagePercentage = usagePercentage,
            wasSummarized = lastOperationWasSummarized,
            summarizationError = lastSummarizationError
        )
    }

    /**
     * Очищает все состояние менеджера контекстного окна
     * Сбрасывает буферы, суммаризацию и статистику
     */
    override fun clear() {
        messageBuffer.clear()
        contextSummary = ContextSummaryInfo(message = "", totalTokens = 0)
        contextInfo = ContextInfo(
            sizeOfSummaryMessages = 0,
            sizeOfActiveMessages = 0,
            sizeOfSystemPrompt = 0
        )
        lastSummarizationError = null
        lastOperationWasSummarized = false
    }

    /**
     * Обновляет contextSummary в текущем активном промпте
     * Вызывается перед отправкой сообщения в API
     */
    fun updatePromptWithSummary(prompt: SystemPrompt<*>) {
        if (contextSummary.message.isNotEmpty()) {
            prompt.contextSummary = contextSummary.message
        }
    }


    /**
     * Подсчитывает общий размер сообщений в токенах
     * Использует данные tokenUsage из сообщения, если доступны,
     * иначе применяет эвристику через TokenEstimator
     */
    private fun calculateMessagesTokenSize(messages: List<Message>): Int {
        return messages.sumOf { message ->
            message.tokenUsage?.totalTokens ?: TokenEstimator.estimateTokenCount(message.text)
        }
    }

    /**
     * Оценивает размер системного промпта в токенах
     * Учитывает базовый размер промпта и contextSummary
     */
    private fun estimateSystemPromptSize(systemPrompt: SystemPrompt<*>): Int {
        // Фиксированные оценки для известных промптов
        val basePromptSize = when (systemPrompt) {
            is PlainTextPrompt -> 0
            is JsonOutputPrompt -> 150
            is BuildComputerAssistantPrompt -> 300
            else -> 100
        }

        val summarySize = systemPrompt.contextSummary?.let {
            TokenEstimator.estimateTokenCount(it)
        } ?: 0

        return basePromptSize + summarySize
    }
}