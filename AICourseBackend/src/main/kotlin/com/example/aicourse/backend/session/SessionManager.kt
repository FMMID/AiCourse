package com.example.aicourse.backend.session

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
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
     * Пытается найти транспорт по ID. Если не находит — ищет любой активный (Fallback).
     */
    suspend fun handleMessage(call: ApplicationCall, sessionId: String?) {
        // 1. Пробуем найти по явному ID
        val transport = if (sessionId != null) activeTransports[sessionId] else null

        if (transport != null) {
            safeHandle(transport, call)
            return
        }

        // 2. Fallback логика (для Инспектора)
        val fallback = activeTransports.values.lastOrNull()
        if (fallback != null) {
            println("⚠️ Forwarding POST to fallback session (Target ID was: $sessionId)")
            safeHandle(fallback, call)
            return
        }

        call.respond(HttpStatusCode.Companion.NotFound, "Session not found")
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