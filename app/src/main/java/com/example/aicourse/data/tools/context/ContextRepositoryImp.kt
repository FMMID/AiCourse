package com.example.aicourse.data.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.tools.context.ContextRepository
import com.example.aicourse.domain.tools.context.TokenEstimator
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

class ContextRepositoryImp(
    private val summarizeContextDataSource: SummarizeContextDataSource
) : ContextRepository {

    override suspend fun summarizeContext(messageHistory: List<Message>): ContextSummaryInfo {
        // Форматируем историю сообщений в строку для суммаризации
        val formattedHistory = messageHistory.joinToString(separator = "\n\n") { message ->
            val role = when (message.type) {
                MessageType.USER -> "Пользователь"
                MessageType.BOT -> "Ассистент"
                MessageType.SYSTEM -> "Система"
            }
            "$role: ${message.text}"
        }

        // Вызываем API для суммаризации
        val summaryText = summarizeContextDataSource.summarizeContext(formattedHistory)

        // Подсчитываем токены в суммаризации используя TokenEstimator
        val estimatedTokens = TokenEstimator.estimateTokenCount(summaryText)

        return ContextSummaryInfo(
            message = summaryText,
            totalTokens = estimatedTokens
        )
    }
}