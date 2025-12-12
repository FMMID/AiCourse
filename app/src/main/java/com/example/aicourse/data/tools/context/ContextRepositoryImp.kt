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
        val contextSummaryInfo = summarizeContextDataSource.summarizeContext(formattedHistory)

        return if (contextSummaryInfo.totalTokens == 0) {
            val estimatedTokens = TokenEstimator.estimateTokenCount(contextSummaryInfo.message)
            ContextSummaryInfo(
                message = contextSummaryInfo.message,
                totalTokens = estimatedTokens
            )
        } else {
            contextSummaryInfo
        }
    }
}