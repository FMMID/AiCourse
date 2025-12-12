package com.example.aicourse.data.chat.remote.huggingface

import android.util.Log
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.data.chat.remote.huggingface.model.HfChatCompletionRequest
import com.example.aicourse.data.chat.remote.huggingface.model.HfChatCompletionResponse
import com.example.aicourse.data.chat.remote.huggingface.model.HfChatMessage
import com.example.aicourse.data.chat.remote.model.ChatResponseData
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.ModelType
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация удаленного источника данных для HuggingFace Router API
 * Использует Ktor для HTTP запросов
 *
 * API совместимо с OpenAI Chat Completions format
 * Документация: https://huggingface.co/docs/api-inference/index
 */
class HuggingFaceDataSource(
    private val apiToken: String
) : BaseChatRemoteDataSource() {

    companion object {
        private const val CHAT_API_URL = "https://router.huggingface.co/v1/chat/completions"
        private const val DEFAULT_MODEL = "openai/gpt-oss-20b:hyperbolic"
        private const val MAX_HISTORY_MESSAGES = 40
    }

    override val logTag: String = "HuggingFaceDataSource"

    /**
     * Резолвит тип модели в конкретный идентификатор модели HuggingFace
     * Используются модели доступные через HuggingFace Router API
     */
    override fun resolveModel(modelType: ModelType): String {
        return when (modelType) {
            ModelType.FAST -> "bineric/NorskGPT-Llama3-8b:featherless-ai" // Быстрая модель (по умолчанию)
            ModelType.BALANCED -> "huihui-ai/Qwen2.5-32B-Instruct-abliterated:featherless-ai" // Сбалансированная
            ModelType.POWERFUL -> "openai/gpt-oss-120b:sambanova" // Мощная модель
        }
    }

    override fun <T> createMessage(role: String, content: String): T {
        @Suppress("UNCHECKED_CAST")
        return HfChatMessage(role = role, content = content) as T
    }

    override suspend fun sendMessage(
        message: String,
        config: ChatConfig,
        messageHistory: List<Message>
    ): ChatResponseData = withContext(Dispatchers.IO) {
        try {
            val messages: List<HfChatMessage> = buildMessagesList(
                systemContent = config.systemContent,
                messageHistory = messageHistory,
                currentMessage = message,
                maxHistoryMessages = MAX_HISTORY_MESSAGES,
                roleSystem = HfChatMessage.ROLE_SYSTEM,
                roleUser = HfChatMessage.ROLE_USER,
                messageTypeToRole = HfChatMessage::fromMessageType
            )

            val request = HfChatCompletionRequest(
                model = config.model ?: DEFAULT_MODEL,
                messages = messages,
                temperature = config.temperature.toDouble(),
                topP = config.topP.toDouble(),
                maxTokens = config.maxTokens,
                stream = false
            )

            Log.d(logTag, "Sending request to HuggingFace API with ${messages.size} messages")

            val response: HfChatCompletionResponse = httpClient.post(CHAT_API_URL) {
                header(HttpHeaders.Authorization, "Bearer $apiToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Пустой ответ от HuggingFace API")

            Log.d(logTag, "Received response from HuggingFace API")

            // Извлекаем статистику токенов из usage (может быть null)
            ChatResponseData(
                content = content,
                promptTokens = response.usage?.promptTokens,
                completionTokens = response.usage?.completionTokens,
                totalTokens = response.usage?.totalTokens,
                modelName = response.model
            )

        } catch (e: Exception) {
            Log.e(logTag, "Error sending message to HuggingFace", e)
            throw Exception("Ошибка отправки сообщения в HuggingFace: ${e.message}", e)
        }
    }

    override suspend fun sendSummarizationRequest(
        systemPrompt: String,
        userMessage: String,
        temperature: Double,
        topP: Double,
        maxTokens: Int
    ): String = withContext(Dispatchers.IO) {
        val messages = listOf(
            HfChatMessage(role = HfChatMessage.ROLE_SYSTEM, content = systemPrompt),
            HfChatMessage(role = HfChatMessage.ROLE_USER, content = userMessage)
        )

        val request = HfChatCompletionRequest(
            model = DEFAULT_MODEL,
            messages = messages,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            stream = false
        )

        Log.d(logTag, "Sending summarization request to HuggingFace API")

        val response: HfChatCompletionResponse = httpClient.post(CHAT_API_URL) {
            header(HttpHeaders.Authorization, "Bearer $apiToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        val summary = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Пустой ответ от HuggingFace API при суммаризации")

        Log.d(logTag, "Summarization completed, tokens: ${response.usage?.totalTokens}")
        return@withContext summary
    }
}
