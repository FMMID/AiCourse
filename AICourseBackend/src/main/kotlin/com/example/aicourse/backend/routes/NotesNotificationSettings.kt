package com.example.aicourse.backend.routes

import com.example.aicourse.backend.services.notes.NotesService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val userId: String,
    val token: String
)

fun Route.notesNotificationSettingRoutes() {
    // Регистрация FCM токена для пользователя
    post("/register-push-token") {
        val params = call.receive<RegistrationRequest>()

        if (params.userId.isNotBlank() && params.token.isNotBlank()) {
            NotesService.updateToken(params.userId, params.token)
            call.respondText("Registered", status = HttpStatusCode.OK)
            println("New FCM Token registered for user: ${params.userId}")
        } else {
            call.respondText("Invalid data", status = HttpStatusCode.BadRequest)
        }
    }
}