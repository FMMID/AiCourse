package com.example.aicourse.backend.routes

import com.example.aicourse.backend.session.SessionManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import kotlinx.coroutines.awaitCancellation

fun Route.mcpRoutes(mcpServer: Server) {

    sse("/sse") {
        println("🔌 New SSE connection established")

        val transport = SseServerTransport("/messages", this)
        SessionManager.register(transport.sessionId, transport)
        val session = mcpServer.createSession(transport)

        session.onClose {
            println("🔌 Session $transport.sessionId disconnected")
            SessionManager.remove(transport.sessionId)
        }

        awaitCancellation()
    }

    route("/messages") {
        options { handleOptions(call) }
        post {
            val sessionId = call.request.queryParameters["sessionId"]
            SessionManager.handleMessage(call, sessionId)
        }
    }
}

suspend fun handleOptions(call: ApplicationCall) {
    call.response.headers.append("Access-Control-Allow-Origin", "*")
    call.response.headers.append("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
    call.response.headers.append("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
    call.respond(HttpStatusCode.OK)
}