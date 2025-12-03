package com.example.aicourse.data.chat.remote.model

import com.example.aicourse.domain.chat.model.MessageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val temperature: Double = 1.0,
    @SerialName("top_p")
    val topP: Double = 0.1,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"

        fun fromMessageType(messageType: MessageType): String {
            return when(messageType) {
                MessageType.USER -> ROLE_USER
                MessageType.BOT -> ROLE_ASSISTANT
            }
        }
    }
}

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
