package com.example.aicourse.backend.notification.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun Server.registerNotificationTools() {
    addTool(
        name = "send_telegram_message",
        description = "Sends a text message to the user via Telegram. Use this to send reports, summaries, or urgent reminders.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("message") {
                    put("type", "string")
                    put("description", "The text content to send")
                }
            },
            required = listOf("message")
        )
    ) { request ->
        val message = request.arguments?.get("message")?.jsonPrimitive?.content
            ?: return@addTool CallToolResult(isError = true, content = listOf(TextContent(text = "Message is empty")))

        // --- ЛОГИКА ОТПРАВКИ ---
        // В реальном проекте тут был бы HTTP запрос к Telegram API
        // Пока просто пишем в консоль, чтобы увидеть в логах Docker
        val logMessage = """
            =========================================
            🚀 TELEGRAM MESSAGE SENT 🚀
            -----------------------------------------
            $message
            =========================================
        """.trimIndent()

        println(logMessage)

        CallToolResult(
            content = listOf(TextContent(text = "Message successfully sent to Telegram."))
        )
    }
}