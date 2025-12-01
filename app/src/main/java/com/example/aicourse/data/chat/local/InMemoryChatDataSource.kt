package com.example.aicourse.data.chat.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * In-memory реализация локального хранилища для истории чата
 * TODO: Заменить на Room Database или SharedPreferences для персистентного хранения
 */
class InMemoryChatDataSource : ChatLocalDataSource {

    private val messageHistory = mutableListOf<String>()

    override suspend fun getMessageHistory(): List<String> = withContext(Dispatchers.IO) {
        messageHistory.toList()
    }

    override suspend fun saveMessage(message: String, isUser: Boolean) = withContext(Dispatchers.IO) {
        val prefix = if (isUser) "USER" else "BOT"
        messageHistory.add("$prefix: $message")
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        messageHistory.clear()
    }
}
