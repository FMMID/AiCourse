package com.example.aicourse.backend.routes

import com.example.aicourse.backend.session.SessionManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import kotlinx.coroutines.awaitCancellation
import java.util.*

fun Route.mcpRoutes(mcpServer: Server) {

    // 1. SSE Подключение
    sse("/sse") {
        println("🔌 New SSE connection established")
        val sessionId = UUID.randomUUID().toString()

        // Формируем корректный URL для клиента
        val scheme = call.request.local.scheme
        val host = call.request.host()
        val port = call.request.port()
        val fullUrl = "$scheme://$host:$port/messages/$sessionId"

        println("🔗 Sending transport URL: $fullUrl")

        val transport = SseServerTransport(fullUrl, this)
        SessionManager.register(sessionId, transport)
        mcpServer.connect(transport)

        try {
            awaitCancellation()
        } finally {
            println("🔌 Session $sessionId disconnected")
            SessionManager.remove(sessionId)
        }
    }

    // 2. Обработка POST сообщений
    // Ловим все варианты путей, чтобы угодить разным клиентам

    // Вариант А: Правильный путь с ID
    route("/messages/{sessionId}") {
        options { handleOptions(call) }
        post {
            val sessionId = call.parameters["sessionId"]
            SessionManager.handleMessage(call, sessionId)
        }
    }

    // Вариант Б: Корневой путь /messages (Fallback)
    route("/messages") {
        options { handleOptions(call) }
        post {
            val sessionId = call.request.queryParameters["sessionId"]
            SessionManager.handleMessage(call, sessionId)
        }
    }

    // Вариант В: Путь /sse (для StreamableHttp клиентов)
    post("/sse") {
        val sessionId = call.request.queryParameters["sessionId"]
        SessionManager.handleMessage(call, sessionId)
    }
    options("/sse") { handleOptions(call) }
}

suspend fun handleOptions(call: ApplicationCall) {
    call.response.headers.append("Access-Control-Allow-Origin", "*")
    call.response.headers.append("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
    call.response.headers.append("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
    call.respond(HttpStatusCode.OK)
}