package com.example.aicourse.backend.services.ai.gigaChat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Модели данных для GigaChat API
 * https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/gigachat-api
 */

// ============ OAuth Token Models ============

@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_at")
    val expiresAt: Long
)

// ============ Chat Completion Models ============

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val functions: List<GigaFunction>? = null, // список доступных функций
    @SerialName("function_call")
    val functionCall: String? = null, // Указываем режим вызова (auto - модель сама решает) "auto" или "none"

    val temperature: Double = 1.0,
    @SerialName("top_p")
    val topP: Double = 0.1,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class GigaFunction(
    val name: String,
    val description: String?,
    // Схема параметров (JSON Schema)
    val parameters: JsonObject
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String? = null,
    @SerialName("function_call")
    val functionCall: FunctionCall? = null,
    val name: String? = null,
    @SerialName("functions_state_id")
    val functionsStateId: String? = null
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"
        const val ROLE_FUNCTION = "function"
    }
}

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: JsonObject
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val usage: Usage? = null,
    val `object`: String? = null
)

@Serializable
data class Choice(
    val message: ChatMessage,
    val index: Int,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)
