package com.example.aicourse.data.chat.repository

import android.content.Context
import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.remote.ChatRemoteDataSource
import com.example.aicourse.data.chat.remote.mapper.SystemPromptMapper
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendMessageResult
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация репозитория чата
 * Координирует работу между удаленным API и локальным хранилищем
 */
class ChatRepositoryImpl(
    private val context: Context,
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatLocalDataSource
) : ChatRepository {

    override suspend fun sendMessage(
        systemPrompt: SystemPrompt<*>,
        messageHistory: List<Message>,
        contextSummaryInfo: ContextSummaryInfo?
    ): Result<SendMessageResult> = withContext(Dispatchers.IO) {
        try {
            val resolvedModel = systemPrompt.modelType?.let { modelType -> remoteDataSource.resolveModel(modelType) }
            val config = SystemPromptMapper.toChatConfig(context, systemPrompt, resolvedModel, contextSummaryInfo)
            val rawResponse = remoteDataSource.sendMessage(config, messageHistory)
            val botResponse = systemPrompt.parseResponse(rawResponse.content)

            val result = SendMessageResult(
                botResponse = botResponse,
                tokenUsage = TokenUsage(
                    promptTokens = rawResponse.promptTokens,
                    completionTokens = rawResponse.completionTokens,
                    totalTokens = rawResponse.totalTokens,
                    maxAvailableTokens = systemPrompt.maxTokens
                ),
                modelName = rawResponse.modelName ?: resolvedModel
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveChatSate(chatStateModel: ChatStateModel): Result<Unit> {
        return try {
            localDataSource.saveChatState(chatStateModel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatState(chatId: String): Result<ChatStateModel> {
        return try {
            val chatState = localDataSource.getChatState(chatId)
            Result.success(chatState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearHistory(): Result<Unit> {
        return try {
            localDataSource.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
