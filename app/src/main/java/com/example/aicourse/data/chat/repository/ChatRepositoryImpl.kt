package com.example.aicourse.data.chat.repository

import com.example.aicourse.data.chat.local.ChatLocalDataSource
import com.example.aicourse.data.chat.remote.ChatRemoteDataSource
import com.example.aicourse.domain.chat.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация репозитория чата
 * Координирует работу между удаленным API и локальным хранилищем
 */
class ChatRepositoryImpl(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatLocalDataSource
) : ChatRepository {

    override suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            localDataSource.saveMessage(message, isUser = true)
            val response = remoteDataSource.sendMessage(message)
            localDataSource.saveMessage(response, isUser = false)
            Result.success(response)
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
