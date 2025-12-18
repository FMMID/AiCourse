package com.example.aicourse.backend.notification.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val telegramBotToken = System.getenv("TELEGRAM_BOT_TOKEN")
private val telegramChatId = System.getenv("TELEGRAM_CHAT_ID")

fun Server.registerNotificationTools() {
    addTool(
        name = "send_telegram_message",
        description = "Sends a text message via Telegram. Use this to send reports to the user.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("message") {
                    put("type", "string")
                    put("description", "The formatted text content to send")
                }
            },
            required = listOf("message")
        )
    ) { request ->
        val message = request.arguments?.get("message")?.jsonPrimitive?.content
            ?: return@addTool CallToolResult(isError = true, content = listOf(TextContent(text = "Message is empty")))

        try {
            val success = sendToTelegram(message)

            val resultText =
                if (success) "Message successfully sent to Telegram." else "Failed to send message to Telegram API."

            println("🚀 TELEGRAM: $resultText\nContent: $message")

            CallToolResult(content = listOf(TextContent(text = resultText)))
        } catch (e: Exception) {
            e.printStackTrace()
            CallToolResult(isError = true, content = listOf(TextContent(text = "Error: ${e.message}")))
        }
    }
}

private fun sendToTelegram(text: String): Boolean {
    val client = HttpClient.newHttpClient()
    // Экранируем текст для JSON (минимально)
    val escapedText = text.replace("\"", "\\\"").replace("\n", "\\n")

    val jsonBody = """
        {
            "chat_id": "$telegramChatId",
            "text": "$escapedText"
        }
    """.trimIndent()

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.telegram.org/bot$telegramBotToken/sendMessage"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.statusCode() == 200
}
