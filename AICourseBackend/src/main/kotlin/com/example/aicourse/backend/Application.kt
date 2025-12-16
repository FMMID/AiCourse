package com.example.aicourse.backend

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
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
// Хранилище сессий
val activeTransports = ConcurrentHashMap<String, SseServerTransport>()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    intercept(ApplicationCallPipeline.Monitoring) {
        val method = call.request.httpMethod.value
        val uri = call.request.uri
        println(">>> REQ: $method $uri")
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-Requested-With")
        allowNonSimpleContentTypes = true
        allowCredentials = true
    }

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
            println("!!! TOOL EXECUTED !!! Status: $requestedStatus")
            CallToolResult(
                content = listOf(TextContent(text = "Found 42 open tasks with status: $requestedStatus"))
            )
        }
    }

    routing {
        // 1. SSE подключение
        sse("/sse") {
            println("New SSE connection established")
            val sessionId = UUID.randomUUID().toString()

            val scheme = call.request.local.scheme
            val host = call.request.host()
            val port = call.request.port()

            // ИЗМЕНЕНИЕ: Используем путь /messages/sessionId вместо ?sessionId=...
            // Это решает проблему с кодировкой %3F
            val fullUrl = "$scheme://$host:$port/messages/$sessionId"

            println("Sending transport URL: $fullUrl")

            val transport = SseServerTransport(fullUrl, this)
            activeTransports[sessionId] = transport
            mcpServer.connect(transport)

            try {
                awaitCancellation()
            } finally {
                println("Session $sessionId disconnected")
                activeTransports.remove(sessionId)
            }
        }

        // 2. Обработка POST сообщений с ID в пути (для SSE)
        route("/messages/{sessionId}") {
            options { handleOptions(call) }
            post {
                val sessionId = call.parameters["sessionId"]
                handlePost(call, sessionId)
            }
        }

        // 3. Обработка POST на корень /messages (если вдруг клиент обрежет ID)
        route("/messages") {
            options { handleOptions(call) }
            post { handlePost(call, null) }
        }

        // 4. Обработка /sse (для StreamableHttp)
        post("/sse") {
            handlePost(call, null)
        }
        options("/sse") {
            handleOptions(call)
        }
    }
}

suspend fun handleOptions(call: ApplicationCall) {
    call.response.headers.append("Access-Control-Allow-Origin", "*")
    call.response.headers.append("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
    call.response.headers.append("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
    call.respond(HttpStatusCode.OK)
}

suspend fun handlePost(call: ApplicationCall, pathSessionId: String?) {
    // Пытаемся найти ID: либо из пути, либо из query (на всякий случай), либо fallback
    val sessionId = pathSessionId ?: call.request.queryParameters["sessionId"]

    // ИЗМЕНЕНИЕ: Безопасное получение транспорта
    // ConcurrentHashMap падает от null, поэтому проверяем sessionId
    val transport = if (sessionId != null) activeTransports[sessionId] else null

    if (transport == null) {
        // Fallback: берем последнюю активную сессию
        val fallback = activeTransports.values.lastOrNull()
        if (fallback != null) {
            println("Forwarding POST to fallback session (Target ID was: $sessionId)")
            try {
                fallback.handlePostMessage(call)
            } catch (e: Exception) {
                println("Error in fallback handlePostMessage:")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
            return
        }

        println("Session not found and no fallback available.")
        call.respond(HttpStatusCode.NotFound, "Session not found")
        return
    }

    try {
        transport.handlePostMessage(call)
    } catch (e: Exception) {
        println("Error in transport handlePostMessage:")
        e.printStackTrace()
        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
    }
}