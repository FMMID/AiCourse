package com.example.aicourse.backend.services.ai.gigaChat

import com.example.aicourse.backend.services.ai.gigaChat.model.ChatCompletionRequest
import com.example.aicourse.backend.services.ai.gigaChat.model.ChatCompletionResponse
import com.example.aicourse.backend.services.ai.gigaChat.model.ChatMessage
import com.example.aicourse.backend.services.ai.gigaChat.model.TokenResponse
import com.example.aicourse.backend.services.notes.AiServiceNotes
import com.example.aicourse.backend.services.notes.Note
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.*

object GigaChatServerClient : AiServiceNotes {
    private const val OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
    private const val CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
    private const val SCOPE = "GIGACHAT_API_PERS"
    private const val DEFAULT_MODEL = "GigaChat"

    private val AUTH_KEY = System.getenv("GIGACHAT_AUTH_KEY")
        ?: throw IllegalStateException("GIGACHAT_AUTH_KEY not found in environment")

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val tokenMutex = Mutex()
    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0

    /**
     * Основной метод для агента: генерация саммари по списку заметок
     */
    override suspend fun generateSummary(notes: List<Note>): String {
        if (notes.isEmpty()) return "Заметок пока нет, планировать нечего!"

        val token = getValidToken()

        // Формируем текст заметок для промпта
        val notesContent = notes.joinToString("\n") { "- ${it.text}" }

        val request = ChatCompletionRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                ChatMessage(
                    role = ChatMessage.ROLE_SYSTEM,
                    content = "Ты личный ассистент. Твоя задача - прочитать список дел пользователя и составить краткую, мотивирующую сводку о том, что ему еще предстоит сделать. Отвечай кратко, 2-3 предложения."
                ),
                ChatMessage(
                    role = ChatMessage.ROLE_USER,
                    content = "Вот мои заметки:\n$notesContent"
                )
            ),
            temperature = 0.7,
            maxTokens = 500
        )

        return try {
            val response: ChatCompletionResponse = httpClient.post(CHAT_API_URL) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            response.choices.firstOrNull()?.message?.content ?: "Не удалось создать саммари."
        } catch (e: Exception) {
            "Ошибка при обращении к GigaChat: ${e.message}"
        }
    }

    /**
     * Логика получения токена (переиспользована из GigaChatDataSource)
     */
    private suspend fun getValidToken(): String = tokenMutex.withLock {
        val currentTime = System.currentTimeMillis()
        if (cachedToken != null && tokenExpiresAt > currentTime + 60_000) {
            return@withLock cachedToken!!
        }

        val response: TokenResponse = httpClient.post(OAUTH_URL) {
            header(HttpHeaders.Authorization, "Basic $AUTH_KEY")
            header("RqUID", UUID.randomUUID().toString())
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build { append("scope", SCOPE) }))
        }.body()

        cachedToken = response.accessToken
        tokenExpiresAt = response.expiresAt
        return@withLock response.accessToken
    }
}