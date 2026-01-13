package com.example.aicourse.domain.tools.context

import android.content.Context
import android.util.Log
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.domain.tools.context.model.ContextWindow
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo
import com.example.aicourse.domain.tools.context.model.HistoryWithSummaryInfo

class ContextWindowManager(
    private val targetContextWindow: ContextWindow,
    private val contextRepository: ContextRepository,
    private val applicationContext: Context
) : Tool<Message> {

    private var contextInfo: ContextInfo = ContextInfo(sizeOfSummaryMessages = 0, sizeOfActiveMessages = 0, sizeOfSystemPrompt = 0)
    private var lastSummarizationError: String? = null
    private var lastOperationWasSummarized: Boolean = false
    private var systemPromptTextSizeMap: MutableMap<String, Int> = mutableMapOf()

    /**
     * Обрабатывает историю сообщений, выполняя суммаризацию при необходимости
     * @param dataForSend данные для отправки, включая историю и активный промпт
     * @return обработанная история сообщений (с возможным сжатием)
     */
    suspend fun processMessageHistory(
        messageHistory: List<Message>,
        activeSystemPrompt: SystemPrompt<*>,
        contextSummaryInfo: ContextSummaryInfo?
    ): HistoryWithSummaryInfo {
        val systemPromptSize = estimateSystemPromptSize(activeSystemPrompt, contextSummaryInfo)
        val activeMessagesSize = calculateMessagesTokenSize(messageHistory)
        val contextSummarySize = contextSummaryInfo?.totalTokens ?: 0

        val totalTokens = contextSummarySize + activeMessagesSize + systemPromptSize
        val needSummary = targetContextWindow.shouldSummarizeDialog(totalTokens)

        if (needSummary && messageHistory.size > targetContextWindow.keepLastMessagesNumber) {
            return performSummarization(messageHistory, contextSummaryInfo, systemPromptSize)
        }

        lastOperationWasSummarized = false

        contextInfo = ContextInfo(
            sizeOfSummaryMessages = contextSummarySize,
            sizeOfActiveMessages = activeMessagesSize,
            sizeOfSystemPrompt = systemPromptSize
        )

        return HistoryWithSummaryInfo(
            messagesForSendToAi = messageHistory,
            contextSummaryInfo = contextSummaryInfo
        )
    }

    /**
     * Выполняет суммаризацию части истории сообщений
     * Оставляет 5 последних сообщений без изменений
     */
    private suspend fun performSummarization(
        messageHistory: List<Message>,
        contextSummaryInfo: ContextSummaryInfo?,
        systemPromptSize: Int
    ): HistoryWithSummaryInfo {
        try {
            val messagesToSummarize = messageHistory.dropLast(targetContextWindow.keepLastMessagesNumber)
            val messagesToRetain = messageHistory.takeLast(targetContextWindow.keepLastMessagesNumber)

            Log.d(
                "fed",
                "messageHistorySize:${messageHistory.size}  messagesToSummarizeSize:${messagesToSummarize.size} messagesToRetain:${messagesToRetain.size} keepLastMessagesNumber:${targetContextWindow.keepLastMessagesNumber}"
            )

            val newSummary = contextRepository.summarizeContext(
                messageHistory = messagesToSummarize,
                existContextSummary = contextSummaryInfo
            )

            lastSummarizationError = null

            val activeMessagesSize = calculateMessagesTokenSize(messagesToRetain)
            contextInfo = ContextInfo(
                sizeOfSummaryMessages = newSummary.totalTokens,
                sizeOfActiveMessages = activeMessagesSize,
                sizeOfSystemPrompt = systemPromptSize
            )

            lastOperationWasSummarized = true

            Log.d("ContextWindowManager", "Context summarized: ${messagesToSummarize.size} messages compressed")
            return HistoryWithSummaryInfo(messagesForSendToAi = messagesToRetain, contextSummaryInfo = newSummary)

        } catch (e: Exception) {
            Log.e("ContextWindowManager", "Summarization failed", e)
            lastSummarizationError = "Ошибка суммаризации: ${e.message}"
            lastOperationWasSummarized = false
            return HistoryWithSummaryInfo(
                messagesForSendToAi = messageHistory,
                contextSummaryInfo = contextSummaryInfo
            )
        }
    }

    /**
     * Обрабатывает полученное сообщение от бота и возвращает статистику контекстного окна
     * @param processData данные полученного сообщения
     * @return ContextWindowInfo с актуальной статистикой использования контекста
     */
    override fun processData(processData: Message): ToolResult {
        val messageTokens = processData.tokenUsage?.totalTokens ?: TokenEstimator.estimateTokenCount(processData.text)

        val updatedActiveSize = contextInfo.sizeOfActiveMessages + messageTokens
        contextInfo = contextInfo.copy(sizeOfActiveMessages = updatedActiveSize)

        val totalUsed = contextInfo.sizeOfSummaryMessages + contextInfo.sizeOfActiveMessages + contextInfo.sizeOfSystemPrompt
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
        contextInfo = ContextInfo(
            sizeOfSummaryMessages = 0,
            sizeOfActiveMessages = 0,
            sizeOfSystemPrompt = 0
        )
        lastSummarizationError = null
        lastOperationWasSummarized = false
    }

    /**
     * Подсчитывает общий размер сообщений в токенах
     * Использует данные tokenUsage из сообщения, если доступны,
     * иначе применяет эвристику через TokenEstimator
     */
    private fun calculateMessagesTokenSize(messages: List<Message>): Int {
        return messages.sumOf { message ->
            if (message.type == MessageType.BOT) {
                message.tokenUsage?.totalTokens ?: TokenEstimator.estimateTokenCount(message.text)
            } else {
                0
            }
        }
    }

    /**
     * Оценивает размер системного промпта в токенах
     * Учитывает базовый размер промпта и contextSummary
     */
    private fun estimateSystemPromptSize(
        activeSystemPrompt: SystemPrompt<*>,
        contextSummaryInfo: ContextSummaryInfo?
    ): Int {
        val systemPrompt = activeSystemPrompt.extractSystemPrompt(applicationContext)

        val textTokens = if (systemPrompt != null) {
            systemPromptTextSizeMap.getOrPut(systemPrompt) {
                TokenEstimator.estimateTokenCount(systemPrompt)
            }
        } else {
            0
        }

        val summaryTokens = contextSummaryInfo?.totalTokens ?: 0
        return textTokens + summaryTokens
    }
}