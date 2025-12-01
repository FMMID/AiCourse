package com.example.aicourse.domain.model

data class Message(
    val id: String,
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    USER,
    BOT
}
