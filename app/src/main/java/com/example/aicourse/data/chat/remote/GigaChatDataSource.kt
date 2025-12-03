package com.example.aicourse.data.chat.remote

import android.util.Log
import com.example.aicourse.data.chat.remote.model.ChatCompletionRequest
import com.example.aicourse.data.chat.remote.model.ChatCompletionResponse
import com.example.aicourse.data.chat.remote.model.ChatMessage
import com.example.aicourse.data.chat.remote.model.TokenResponse
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
) : ChatRemoteDataSource {

    companion object {
        private const val TAG = "GigaChatDataSource"
        private const val OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
        private const val CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1"
        private const val DEFAULT_MODEL = "GigaChat"
        private const val SCOPE = "GIGACHAT_API_PERS"
        private const val MAX_HISTORY_MESSAGES = 40
    }

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0
    private val tokenMutex = Mutex()

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.HEADERS
        }
    }

    override suspend fun sendMessage(
        message: String,
        config: ChatConfig,
        messageHistory: List<Message>
    ): String = withContext(Dispatchers.IO) {
        try {
            val token = getValidToken()
            val recentHistory = messageHistory.takeLast(MAX_HISTORY_MESSAGES)
            val messages = buildList {
                config.systemContent?.let { systemContent ->
                    add(
                        ChatMessage(
                            role = ChatMessage.ROLE_SYSTEM,
                            content = systemContent
                        )
                    )
                }

                recentHistory.forEach { msg ->
                    add(ChatMessage(role = ChatMessage.fromMessageType(msg.type), content = msg.text))
                }

                add(
                    ChatMessage(
                        role = ChatMessage.ROLE_USER,
                        content = message
                    )
                )
            }

            val request = ChatCompletionRequest(
                model = DEFAULT_MODEL,
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

            response.choices.firstOrNull()?.message?.content ?: throw Exception("Пустой ответ от GigaChat API")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to GigaChat", e)
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

        Log.d(TAG, "Requesting new OAuth token from GigaChat")
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

            Log.d(TAG, "Successfully obtained OAuth token, expires at: ${response.expiresAt}")
            return response

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching OAuth token", e)
            throw Exception("Ошибка получения токена авторизации: ${e.message}", e)
        }
    }

    /**
     * Закрывает HTTP клиент
     */
    fun close() {
        httpClient.close()
    }
}
