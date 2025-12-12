package com.example.aicourse.data.chat.remote.huggingface.model

import com.example.aicourse.domain.chat.model.MessageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Модели данных для HuggingFace Router API
 * API совместимо с OpenAI Chat Completions format
 * https://huggingface.co/docs/api-inference/index
 */

// ============ Chat Completion Models ============

@Serializable
data class HfChatCompletionRequest(
    val model: String,
    val messages: List<HfChatMessage>,
    val temperature: Double = 0.7,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class HfChatMessage(
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
                MessageType.SYSTEM -> ROLE_SYSTEM
            }
        }
    }
}

@Serializable
data class HfChatCompletionResponse(
    val id: String? = null,
    val choices: List<HfChoice>,
    val created: Long? = null,
    val model: String? = null,
    val usage: HfUsage? = null,
    @SerialName("object")
    val objectType: String? = null
)

@Serializable
data class HfChoice(
    val index: Int,
    val message: HfChatMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class HfUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)
