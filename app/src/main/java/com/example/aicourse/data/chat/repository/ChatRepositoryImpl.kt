package com.example.aicourse.data.chat.repository

import android.content.Context
import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.mapper.SystemPromptMapper
import com.example.aicourse.data.chat.remote.ChatRemoteDataSource
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SystemPrompt
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.repository.ChatRepository
import com.example.aicourse.domain.chat.repository.SendMessageResult
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
        message: String,
        systemPrompt: SystemPrompt<*>,
        messageHistory: List<Message>
    ): Result<SendMessageResult> = withContext(Dispatchers.IO) {
        try {
            localDataSource.saveMessage(message, isUser = true)

            val resolvedModel = systemPrompt.modelType?.let { modelType -> remoteDataSource.resolveModel(modelType) }
            val config = SystemPromptMapper.toChatConfig(context, systemPrompt, resolvedModel)
            val rawResponse = remoteDataSource.sendMessage(message, config, messageHistory)
            val botResponse = systemPrompt.parseResponse(rawResponse.content)
            localDataSource.saveMessage(botResponse.rawContent, isUser = false)

            val result = SendMessageResult(
                botResponse = botResponse,
                tokenUsage = TokenUsage(
                    promptTokens = rawResponse.promptTokens,
                    completionTokens = rawResponse.completionTokens,
                    totalTokens = rawResponse.totalTokens
                ),
                modelName = rawResponse.modelName ?: resolvedModel
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessageHistory(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val history = localDataSource.getMessageHistory()
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMessage(message: String, isUser: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localDataSource.saveMessage(message, isUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localDataSource.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
