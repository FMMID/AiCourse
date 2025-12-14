package com.example.aicourse.data.chat.remote.gigachat

import android.util.Log
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.data.chat.remote.gigachat.model.ChatCompletionRequest
import com.example.aicourse.data.chat.remote.gigachat.model.ChatCompletionResponse
import com.example.aicourse.data.chat.remote.gigachat.model.ChatMessage
import com.example.aicourse.data.chat.remote.gigachat.model.TokenResponse
import com.example.aicourse.data.chat.remote.model.ChatResponseData
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.ModelType
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Реализация удаленного источника данных для GigaChat API
 * Использует Ktor для HTTP запросов
 *
 * Документация API:
 * - OAuth: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
 * - Chat: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-chat
 */
class GigaChatDataSource(
    private val authorizationKey: String
) : BaseChatRemoteDataSource() {

    companion object {
        private const val OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
        private const val CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1"
        private const val DEFAULT_MODEL = "GigaChat"
        private const val SCOPE = "GIGACHAT_API_PERS"
    }

    override val logTag: String = "GigaChatDataSource"

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0
    private val tokenMutex = Mutex()

    /**
     * Резолвит тип модели в конкретный идентификатор модели GigaChat
     * Примечание: GigaChat может иметь разные модели (GigaChat, GigaChat-Plus, GigaChat-Pro)
     * На данный момент используем одну модель для всех типов
     */
    override fun resolveModel(modelType: ModelType): String {
        return when (modelType) {
            ModelType.Fast -> "GigaChat" // Быстрая модель
            ModelType.Balanced -> "GigaChat-Pro" // Сбалансированная (пока та же)
            ModelType.Powerful -> "GigaChat-Max" // Мощная (можно использовать GigaChat-Pro если доступна)
        }
    }

    override fun <T> createMessage(role: String, content: String): T {
        @Suppress("UNCHECKED_CAST")
        return ChatMessage(role = role, content = content) as T
    }

    override suspend fun sendMessage(
        config: ChatConfig,
        messageHistory: List<Message>
    ): ChatResponseData = withContext(Dispatchers.IO) {
        try {
            val token = getValidToken()

            val messages: List<ChatMessage> = buildMessagesList(
                systemContent = config.systemContent,
                messageHistory = messageHistory,
                roleSystem = ChatMessage.ROLE_SYSTEM,
                messageTypeToRole = ChatMessage::fromMessageType
            )

            val request = ChatCompletionRequest(
                model = config.model ?: DEFAULT_MODEL,
                messages = messages,
                temperature = config.temperature.toDouble(),
                topP = config.topP.toDouble(),
                maxTokens = config.maxTokens
            )

            val response: ChatCompletionResponse = httpClient.post("$CHAT_API_URL/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Пустой ответ от GigaChat API")

            // Извлекаем статистику токенов из usage (может быть null)
            ChatResponseData(
                content = content,
                promptTokens = response.usage?.promptTokens,
                completionTokens = response.usage?.completionTokens,
                totalTokens = response.usage?.totalTokens,
                modelName = response.model
            )
        } catch (e: Exception) {
            Log.e(logTag, "Error sending message to GigaChat", e)
            throw Exception("Ошибка отправки сообщения: ${e.message}", e)
        }
    }

    /**
     * Получает валидный токен (использует кэшированный или запрашивает новый)
     */
    private suspend fun getValidToken(): String = tokenMutex.withLock {
        val currentTime = System.currentTimeMillis()

        if (cachedToken != null && tokenExpiresAt > currentTime + 60_000) {
            return@withLock cachedToken!!
        }

        Log.d(logTag, "Requesting new OAuth token from GigaChat")
        val newToken = fetchOAuthToken()
        cachedToken = newToken.accessToken
        tokenExpiresAt = newToken.expiresAt

        return@withLock newToken.accessToken
    }

    /**
     * Запрашивает OAuth токен из GigaChat API
     * https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
     */
    private suspend fun fetchOAuthToken(): TokenResponse {
        try {
            val rqUID = UUID.randomUUID().toString()

            val response: TokenResponse = httpClient.post(OAUTH_URL) {
                header(HttpHeaders.Authorization, "Basic $authorizationKey")
                header("RqUID", rqUID)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build { append("scope", SCOPE) }))
            }.body()

            Log.d(logTag, "Successfully obtained OAuth token, expires at: ${response.expiresAt}")
            return response

        } catch (e: Exception) {
            Log.e(logTag, "Error fetching OAuth token", e)
            throw Exception("Ошибка получения токена авторизации: ${e.message}", e)
        }
    }

    override suspend fun sendSummarizationRequest(
        systemPrompt: String,
        messageHistory: List<Message>,
        temperature: Double,
        topP: Double,
        maxTokens: Int
    ): ContextSummaryInfo = withContext(Dispatchers.IO) {
        val token = getValidToken()

        val messages = buildMessagesList<ChatMessage>(
            systemContent = systemPrompt,
            messageHistory = messageHistory,
            roleSystem = ChatMessage.ROLE_SYSTEM,
            messageTypeToRole = ChatMessage::fromMessageType
        )

        val request = ChatCompletionRequest(
            model = DEFAULT_MODEL,
            messages = messages,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens
        )

        val response: ChatCompletionResponse = httpClient.post("$CHAT_API_URL/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        val summary = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("Пустой ответ от GigaChat API при суммаризации")

        Log.d(logTag, "Summarization completed, tokens: ${response.usage?.totalTokens}")
        return@withContext ContextSummaryInfo(summary, response.usage?.totalTokens ?: 0)
    }
}
