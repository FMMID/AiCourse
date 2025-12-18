package com.example.aicourse.backend

import com.example.aicourse.backend.plugins.configureHttp
import com.example.aicourse.backend.plugins.createMcpServer
import com.example.aicourse.backend.routes.mcpRoutes
import com.example.aicourse.backend.routes.notesNotificationSettingRoutes
import com.example.aicourse.backend.services.startServices
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // 1. Конфигурация плагинов
    configureHttp() // CORS + Monitoring

    // 2. Инициализация MCP сервера
    val mcpServer = createMcpServer()

    // 3. Маршрутизация
    routing {
        mcpRoutes(mcpServer)
        notesNotificationSettingRoutes()
    }

    // 4. Запуск дополнительных сервисов
    startServices()
}