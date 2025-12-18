package com.example.aicourse.data.chat.remote.gigachat

import android.util.Log
import com.example.aicourse.data.chat.remote.BaseChatRemoteDataSource
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.data.chat.remote.gigachat.model.ChatCompletionRequest
import com.example.aicourse.data.chat.remote.gigachat.model.ChatCompletionResponse
import com.example.aicourse.data.chat.remote.gigachat.model.ChatMessage
import com.example.aicourse.data.chat.remote.gigachat.model.GigaFunction
import com.example.aicourse.data.chat.remote.gigachat.model.TokenResponse
import com.example.aicourse.data.chat.remote.model.ChatResponseData
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.ModelType
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.mcpclient.McpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
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
    private val authorizationKey: String,
    private val mcpClient: McpClient,
    private val userId: String
) : BaseChatRemoteDataSource() {

    companion object {
        private const val OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
        private const val CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1"
        private const val DEFAULT_MODEL = "GigaChat"
        private const val SCOPE = "GIGACHAT_API_PERS"
    }

    override val logTag: String = "GigaChatDataSource"

    private val json = Json { ignoreUnknownKeys = true }
    private val tokenMutex = Mutex()

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0

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
        return@withContext sendMessageInternal(config, messageHistory, recursionDepth = 0)
    }

    private suspend fun sendMessageInternal(
        config: ChatConfig,
        messageHistory: List<Message>,
        recursionDepth: Int,
        additionalMessages: List<ChatMessage> = emptyList(), // Для хранения цепочки вызовов функций
        forceTextMode: Boolean = false // [FIX] Флаг для принудительного отключения функций при зацикливании
    ): ChatResponseData {
        if (recursionDepth > 5) throw Exception("Too many function calls")

        try {
            val token = getValidToken()

            val baseMessages: List<ChatMessage> = buildMessagesList(
                systemContent = config.systemContent,
                messageHistory = messageHistory,
                roleSystem = ChatMessage.ROLE_SYSTEM,
                messageTypeToRole = ChatMessage::fromMessageType
            )

            val fullMessages = baseMessages + additionalMessages

            mcpClient.connect()

            // TODO: Можно кэшировать tools, чтобы не дергать сервер каждый раз
            val mcpTools = try {
                mcpClient.getTools()
            } catch (e: Exception) {
                Log.e(logTag, "Failed to get MCP tools", e)
                emptyList()
            }

            val gigaFunctions = if (mcpTools.isNotEmpty()) {
                mcpTools.map { tool ->
                    val schemaString = json.encodeToString(ToolSchema.serializer(), tool.inputSchema)
                    val rawParameters = json.parseToJsonElement(schemaString).jsonObject

                    val fixedParameters = if (!rawParameters.containsKey("properties")) {
                        JsonObject(rawParameters + ("properties" to JsonObject(emptyMap())))
                    } else {
                        rawParameters
                    }

                    GigaFunction(
                        name = tool.name,
                        description = tool.description,
                        parameters = fixedParameters
                    )
                }
            } else null

            val functionCallMode = if (forceTextMode) {
                "none"
            } else if (gigaFunctions != null) {
                "auto"
            } else {
                null
            }

            val request = ChatCompletionRequest(
                model = config.model ?: DEFAULT_MODEL,
                messages = fullMessages,
                functions = if (forceTextMode) null else gigaFunctions, // Не передаем функции, если форсируем текст (для надежности)
                functionCall = functionCallMode,
                temperature = config.temperature.toDouble(),
                topP = config.topP.toDouble(),
                maxTokens = config.maxTokens
            )

            val httpResponse = httpClient.post("$CHAT_API_URL/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)

                Log.d(logTag, "Sending JSON to GigaChat: $request")
            }

            if (httpResponse.status.value != 200) {
                val errorText = httpResponse.bodyAsText()
                Log.e(logTag, "CRITICAL API ERROR: $errorText")
                throw Exception("API Error: $errorText")
            }

            val response: ChatCompletionResponse = httpResponse.body()
            val choice = response.choices.firstOrNull() ?: throw Exception("Empty response")
            val message = choice.message // Тут теперь есть functions_state_id внутри

            if (message.functionCall != null) {
                val functionName = message.functionCall.name
                val argsObject = message.functionCall.arguments
                val stateId = message.functionsStateId
                if (stateId.isNullOrBlank()) {
                    Log.e(logTag, "CRITICAL: functions_state_id is MISSING in GigaChat response! Request chain will fail.")
                } else {
                    Log.d(logTag, "Captured functions_state_id: $stateId")
                }
                Log.d(logTag, "Calling MCP tool: $functionName")


                val lastCall = additionalMessages.lastOrNull { it.role == ChatMessage.ROLE_ASSISTANT }
                if (lastCall != null && lastCall.functionCall != null &&
                    lastCall.functionCall.name == functionName &&
                    lastCall.functionCall.arguments.toString() == argsObject.toString()
                ) {

                    Log.w(logTag, "⚠️ Loop detected for tool '$functionName'. Forcing text response.")
                    return sendMessageInternal(config, messageHistory, recursionDepth, additionalMessages, forceTextMode = true)
                }

                val argsMap = argsObject.entries.associate { (key, element) ->
                    key to element.toPrimitiveValue()
                }.toMutableMap()

                argsMap["userId"] = userId

                Log.d(logTag, "Injected userId into tool arguments: $userId")

                val mcpResult = mcpClient.callTool(functionName, argsMap)
                val toolResultRawText = mcpResult.content.joinToString("\n") {
                    (it as? io.modelcontextprotocol.kotlin.sdk.types.TextContent)?.text ?: ""
                }

                val toolResultJsonString = json.encodeToString(mapOf("result" to toolResultRawText))
                val newAdditionalMessages = additionalMessages.toMutableList().apply {

                    add(
                        message.copy(
                            content = null,
                            functionsStateId = stateId // Явно передаем ID
                        )
                    )

                    add(
                        ChatMessage(
                            role = ChatMessage.ROLE_FUNCTION,
                            name = functionName,
                            content = toolResultJsonString,
                        )
                    )
                }

                return sendMessageInternal(config, messageHistory, recursionDepth + 1, newAdditionalMessages)
            }
            return ChatResponseData(
                content = message.content ?: "", // Content может быть null при function_call
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

    private fun JsonElement.toPrimitiveValue(): Any? {
        return when (this) {
            is JsonPrimitive -> {
                if (isString) content
                else if (booleanOrNull != null) boolean
                else if (intOrNull != null) int
                else if (doubleOrNull != null) double
                else content
            }

            is JsonArray -> map { it.toPrimitiveValue() }
            is JsonObject -> entries.associate { it.key to it.value.toPrimitiveValue() }
            is JsonNull -> null
            else -> toString()
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