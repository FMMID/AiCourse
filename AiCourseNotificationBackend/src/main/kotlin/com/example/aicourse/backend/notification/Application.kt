package com.example.aicourse.backend.notification

import com.example.aicourse.backend.notification.plugins.configureHttp
import com.example.aicourse.backend.notification.routes.mcpRoutes
import com.example.aicourse.backend.notification.tools.registerNotificationTools
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Настройка http
    configureHttp()

    // 2. Создаем MCP сервер
    val mcpServer = Server(
        serverInfo = Implementation("NotificationServer", "1.0.0"),
        options = io.modelcontextprotocol.kotlin.sdk.server.ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    // 3. Регистрируем инструмент (который мы создали ранее)
    mcpServer.registerNotificationTools()

    // 4. Подключаем роутинг (используя скопированный файл)
    routing {
        mcpRoutes(mcpServer)
    }

    println("🚀 Notification MCP Server started on port 8081")
}