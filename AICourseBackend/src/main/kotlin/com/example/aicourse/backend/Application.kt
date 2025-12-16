package com.example.aicourse.backend

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// Хранилище сессий
val activeTransports = ConcurrentHashMap<String, SseServerTransport>()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
    }
    install(SSE)

    val mcpServer = Server(
        serverInfo = Implementation("BackendMcpClient", "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    ) {
        addTool(
            name = "get_tracker_tasks",
            description = "Get count of open tasks from Tracker",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("status") {
                        put("type", "string")
                        put("description", "Task status filter (optional)")
                    }
                }
            )
        ) { arguments ->
            val requestedStatus = arguments.arguments?.get("status")?.jsonPrimitive?.content ?: "all"

            CallToolResult(
                content = listOf(
                    TextContent(
                        text = "Found 42 open tasks with status: $requestedStatus"
                    )
                )
            )
        }
    }

    routing {
        // SSE Endpoint
        sse("/sse") {
            println("New SSE connection...")
            val sessionId = UUID.randomUUID().toString()
            val transport = SseServerTransport("/messages?sessionId=$sessionId", this)
            activeTransports[sessionId] = transport
            // Подключаем транспорт (он начнет слать события в сессию)
            mcpServer.connect(transport)

            try {

                awaitCancellation()
            } catch (e: Exception) {
                // Игнорируем штатное завершение
            } finally {
                println("Session $sessionId disconnected")
                activeTransports.remove(sessionId)
                transport.close()
            }
        }

        // POST Endpoint
        post("/messages") {
            val sessionId = call.request.queryParameters["sessionId"]
            val transport = activeTransports[sessionId]

            if (transport == null) {
                val fallbackTransport = activeTransports.values.lastOrNull()
                if (fallbackTransport != null) {
                    fallbackTransport.handlePostMessage(call)
                    return@post
                }

                call.respond(HttpStatusCode.NotFound, "Session not found")
                return@post
            }
            transport.handlePostMessage(call)
        }
    }
}