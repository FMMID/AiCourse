package com.example.aicourse.backend.notification.session

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import java.util.concurrent.ConcurrentHashMap

object SessionManager {
    private val activeTransports = ConcurrentHashMap<String, SseServerTransport>()

    fun register(sessionId: String, transport: SseServerTransport) {
        activeTransports[sessionId] = transport
    }

    fun remove(sessionId: String) {
        activeTransports.remove(sessionId)
    }

    /**
     * Пытается найти транспорт по ID. Если не находит — ошибка HttpStatusCode.NotFound
     */
    suspend fun handleMessage(call: ApplicationCall, sessionId: String?) {
        val transport = if (sessionId != null) activeTransports[sessionId] else null

        if (transport != null) {
            safeHandle(transport, call)
            return
        }

        call.respond(HttpStatusCode.NotFound, "Session not found")
    }

    private suspend fun safeHandle(transport: SseServerTransport, call: ApplicationCall) {
        try {
            transport.handlePostMessage(call)
        } catch (e: Exception) {
            println("❌ Error handling message: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.Companion.InternalServerError, e.message ?: "Unknown error")
        }
    }
}